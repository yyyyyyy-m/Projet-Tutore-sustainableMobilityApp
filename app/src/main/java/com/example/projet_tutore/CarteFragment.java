package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "CARTE_DEBUG";
    private FusedLocationProviderClient fusedLocationClient;
    private BottomSheetBehavior<View> behavior;
    private FirebaseFirestore db;
    private boolean userRoutesLoaded = false;

    private Button btnCalculer;
    private EditText etDepart, etArrivee;
    private ImageButton btnModeBus, btnModeMarche, btnModeVelo;

    private RecyclerView rvTousItineraires;
    private RouteOptionsAdapter adapter;

    private List<RouteOption> allRoutes = new ArrayList<>();

    private String travelMode = "transit";

    private LatLng originLatLng, destLatLng;
    private Polyline currentPolyline;

    private int completedRequests = 0;
    private LatLng selectedFirebaseDestinationLatLng = null;

    private final String API_KEY = BuildConfig.MAPS_API_KEY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_carte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {

        btnCalculer = v.findViewById(R.id.btnCalculerTrajet);
        etDepart = v.findViewById(R.id.etDepart);
        etArrivee = v.findViewById(R.id.etArrivee);

        btnModeBus = v.findViewById(R.id.btnModeBus);
        btnModeMarche = v.findViewById(R.id.btnModeMarche);
        btnModeVelo = v.findViewById(R.id.btnModeVelo);

        rvTousItineraires = v.findViewById(R.id.rvTousItineraires);
        rvTousItineraires.setLayoutManager(new LinearLayoutManager(requireContext()));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();

        View bottomSheet = v.findViewById(R.id.bottomSheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        btnCalculer.setOnClickListener(view -> {
            Log.d(TAG, "Bouton calcul cliqué");
            calculerTousLesItineraires();
        });

        btnModeBus.setOnClickListener(v1 -> {
            travelMode = "transit";
            updateButtons(btnModeBus);
            filtrerRoutes();
        });

        btnModeMarche.setOnClickListener(v12 -> {
            travelMode = "walking";
            updateButtons(btnModeMarche);
            filtrerRoutes();
        });

        btnModeVelo.setOnClickListener(v13 -> {
            travelMode = "bicycling";
            updateButtons(btnModeVelo);
            filtrerRoutes();
        });
        ArrayAdapter<String> adapterDepart =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);

        ArrayAdapter<String> adapterArrivee =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);

        ((AutoCompleteTextView) etDepart).setAdapter(adapterDepart);
        ((AutoCompleteTextView) etDepart).setThreshold(1);
        ((AutoCompleteTextView) etArrivee).setAdapter(adapterArrivee);
        ((AutoCompleteTextView) etArrivee).setThreshold(1);
        etDepart.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) {
                    fetchAddressSuggestionsOSM(s.toString(), adapterDepart);
                }
            }
        });

        etArrivee.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    fetchDestinationSuggestions(s.toString(), adapterArrivee,
                            (AutoCompleteTextView) etArrivee);
                }
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    private void updateButtons(ImageButton selected) {
        btnModeBus.setBackgroundColor(Color.WHITE);
        btnModeMarche.setBackgroundColor(Color.WHITE);
        btnModeVelo.setBackgroundColor(Color.WHITE);
        selected.setBackgroundColor(Color.parseColor("#B8E6C1"));
    }
    private void fetchAddressSuggestionsOSM(String query, ArrayAdapter<String> adapter) {
        try {
            String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");

            String url = "https://nominatim.openstreetmap.org/search?q="
                    + encodedQuery
                    + "&format=jsonv2"
                    + "&addressdetails=1"
                    + "&limit=10"
                    + "&countrycodes=fr"
                    + "&accept-language=fr";

            new Thread(() -> {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

                    conn.setRequestProperty("User-Agent", "GreenGo-Android-App/1.0");
                    conn.connect();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );

                    StringBuilder json = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }

                    JSONArray results = new JSONArray(json.toString());
                    List<String> suggestions = new ArrayList<>();

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject obj = results.getJSONObject(i);
                        String displayName = obj.getString("display_name");
                        suggestions.add(displayName);
                    }

                    requireActivity().runOnUiThread(() -> {
                        adapter.clear();
                        adapter.addAll(suggestions);
                        adapter.notifyDataSetChanged();
                    });

                } catch (Exception e) {
                    Log.e(TAG, "Erreur OSM autocomplete", e);
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Erreur encodage URL OSM", e);
        }
    }

    private void fetchDestinationSuggestions(String query,
                                             ArrayAdapter<String> adapter,
                                             AutoCompleteTextView autoCompleteTextView) {

        String queryLower = query.toLowerCase(Locale.ROOT).trim();

        db.collection("itineraires_utilisateurs")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<String> suggestions = new ArrayList<>();
                    Map<String, LatLng> destinationCoords = new HashMap<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String destination = document.getString("destination");
                        Double endLat = document.getDouble("endLat");
                        Double endLng = document.getDouble("endLng");

                        if (destination == null || destination.trim().isEmpty()
                                || endLat == null || endLng == null) {
                            continue;
                        }

                        String destinationLower = destination.toLowerCase(Locale.ROOT);

                        if (destinationLower.contains(queryLower)) {
                            if (!suggestions.contains(destination)) {
                                suggestions.add(destination);
                                destinationCoords.put(destination, new LatLng(endLat, endLng));
                            }
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        adapter.clear();
                        adapter.addAll(suggestions);
                        adapter.notifyDataSetChanged();

                        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                            String selectedDestination = parent.getItemAtPosition(position).toString();
                            selectedFirebaseDestinationLatLng = destinationCoords.get(selectedDestination);

                            Log.d(TAG, "Destination Firebase sélectionnée: "
                                    + selectedDestination + " -> " + selectedFirebaseDestinationLatLng);
                        });

                        if (!suggestions.isEmpty() && autoCompleteTextView.hasFocus()) {
                            autoCompleteTextView.showDropDown();
                        }

                        Log.d(TAG, "Suggestions Firebase destination: " + suggestions.size());
                    });
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur suggestions Firebase destination", e)
                );
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        getCurrentLocation(); // Chargement carte = Géolocalise
    }
    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        Log.d(TAG, "POSITION ACTUELLE: " + lat + ", " + lng);

                        originLatLng = new LatLng(lat, lng);

                        // Effet de notre caméra
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 14));

                        Toast.makeText(getContext(),
                                "Position récupérée",
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Log.e(TAG, "Localisation null");
                    }
                });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void calculerTousLesItineraires() {

        String depart = etDepart.getText().toString();
        String arrivee = etArrivee.getText().toString();

        if (depart.isEmpty()) {
            getCurrentLocation();
        } else {
            originLatLng = getLocationFromAddress(depart);
        }
        if (selectedFirebaseDestinationLatLng != null) {
            destLatLng = selectedFirebaseDestinationLatLng;
            Log.d(TAG, "Destination utilisée depuis Firebase: " + destLatLng);
        } else {
            destLatLng = getLocationFromAddress(arrivee);
        }

        if (originLatLng == null || destLatLng == null) {
            Toast.makeText(getContext(), "Adresse invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        completedRequests = 0;
        userRoutesLoaded = false;
        allRoutes.clear();

        String[] modes = {"transit", "driving", "walking", "bicycling"};

        for (String mode : modes) {
            fetchRoute(mode);
            Log.d(TAG, "Départ: " + depart);
            Log.d(TAG, "Arrivée: " + arrivee);
            Log.d(TAG, "Origin: " + originLatLng);
            Log.d(TAG, "Destination: " + destLatLng);
        }
        loadUserSuggestedRoutes();
    }
    private void tryFinishLoadingRoutes() {
        if (completedRequests == 4 && userRoutesLoaded) {
            Log.d(TAG, "TOTAL ROUTES RECUPEREES: " + allRoutes.size());
            filtrerRoutes();
        }
    }

    private void fetchRoute(String mode) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + originLatLng.latitude + "," + originLatLng.longitude
                + "&destination=" + destLatLng.latitude + "," + destLatLng.longitude
                + "&mode=" + mode
                + "&alternatives=true"
                + (mode.equals("transit") ? "&departure_time=now" : "")
                + "&key=" + API_KEY;

        new Thread(() -> {
            try {
                Log.d(TAG, "====================");
                Log.d(TAG, "MODE: " + mode);
                Log.d(TAG, "URL: " + url);

                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                // LOG JSON COMPLET
                Log.d(TAG, "JSON RESPONSE: " + json.toString());

                JSONObject data = new JSONObject(json.toString());
                String status = data.getString("status");

                Log.d(TAG, "STATUS API: " + status);

                if (status.equals("OK")) {

                    JSONArray routes = data.getJSONArray("routes");
                    Log.d(TAG, "NB ROUTES: " + routes.length());

                    for (int i = 0; i < routes.length(); i++) {

                        JSONObject route = routes.getJSONObject(i);
                        JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

                        String duration = leg.getJSONObject("duration").getString("text");
                        String distance = leg.getJSONObject("distance").getString("text");

                        Log.d(TAG, "Route " + i + " -> " + duration + " | " + distance);

                        String polyline = route.getJSONObject("overview_polyline").getString("points");
                        List<LatLng> points = decodePolyline(polyline);

                        RouteOption r = new RouteOption(mode, duration, distance, "", points);

                        List<RouteStep> steps = extractSteps(leg, mode);
                        r.setSteps(steps);

                        if (mode.equals("transit")) {
                            int transfers = countTransfers(leg);
                            r.setNumberOfTransfers(transfers);

                            if (leg.has("fare")) {
                                String fare = leg.getJSONObject("fare").getString("text");
                                r.setPrice(fare);
                            }
                        }

                        synchronized (allRoutes) {
                            allRoutes.add(r);
                        }
                    }

                } else {
                    Log.e(TAG, " ERREUR API: " + status);
                }

            } catch (Exception e) {
                Log.e(TAG, " ERREUR RESEAU", e);
            } finally {
                requireActivity().runOnUiThread(() -> {
                    completedRequests++;
                    Log.d(TAG, "REQUÊTES FINIES: " + completedRequests + "/4");
                    tryFinishLoadingRoutes();
                });
            }
        }).start();
    }
    private List<RouteStep> extractSteps(JSONObject leg, String mode) throws Exception {
        List<RouteStep> steps = new ArrayList<>();
        JSONArray stepsArray = leg.getJSONArray("steps");

        for (int i = 0; i < stepsArray.length(); i++) {
            JSONObject stepObj = stepsArray.getJSONObject(i);
            String travelMode = stepObj.getString("travel_mode");
            String instructions = stepObj.getString("html_instructions").replaceAll("<[^>]*>", "");
            String stepDuration = stepObj.getJSONObject("duration").getString("text");

            if (travelMode.equals("TRANSIT")) {
                JSONObject transitDetails = stepObj.getJSONObject("transit_details");
                JSONObject line = transitDetails.getJSONObject("line");

                String lineName = line.optString("short_name", line.getString("name"));
                String headsign = transitDetails.getString("headsign");
                String departureStop = transitDetails.getJSONObject("departure_stop").getString("name");
                String arrivalStop = transitDetails.getJSONObject("arrival_stop").getString("name");
                int numStops = transitDetails.getInt("num_stops");
                String vehicleType = line.getJSONObject("vehicle").getString("type");
                String lineColor = line.optString("color", "8ED38A");

                String title = lineName + " - Direction " + headsign;
                String details = departureStop + " → " + arrivalStop + " (" + numStops + " arrêts)";

                RouteStep step = new RouteStep("TRANSIT", title, details, stepDuration);
                step.setLineName(lineName);
                step.setLineColor(lineColor);
                step.setVehicleType(vehicleType);

                steps.add(step);

            } else if (travelMode.equals("WALKING")) {
                RouteStep step = new RouteStep("WALKING", "Marche à pied", instructions, stepDuration);
                steps.add(step);
            }
        }

        return steps;
    }

    // COMPTER LES CORRESPONDANCES
    private int countTransfers(JSONObject leg) throws Exception {
        JSONArray steps = leg.getJSONArray("steps");
        int transitCount = 0;

        for (int i = 0; i < steps.length(); i++) {
            if (steps.getJSONObject(i).getString("travel_mode").equals("TRANSIT")) {
                transitCount++;
            }
        }

        return Math.max(0, transitCount - 1);
    }
    private int parseDuration(String duration) {
        try {
            int total = 0;

            if (duration.contains("hour")) {
                String[] parts = duration.split("hour");
                total += Integer.parseInt(parts[0].trim()) * 60;

                if (parts.length > 1 && parts[1].contains("min")) {
                    String mins = parts[1].replaceAll("[^0-9]", "");
                    if (!mins.isEmpty()) {
                        total += Integer.parseInt(mins);
                    }
                }
            } else if (duration.contains("min")) {
                String mins = duration.replaceAll("[^0-9]", "");
                total = Integer.parseInt(mins);
            }

            return total;

        } catch (Exception e) {
            return 9999;
        }
    }

    private void filtrerRoutes() {

        List<RouteOption> filtered = new ArrayList<>();

        Log.d(TAG, "===== FILTRAGE ROUTES =====");
        Log.d(TAG, "Mode sélectionné: " + travelMode);
        Log.d(TAG, "Nombre total allRoutes: " + allRoutes.size());

        for (RouteOption r : allRoutes) {
            Log.d(TAG, "Route disponible: " + r.getMode()
                    + " | " + r.getDuration()
                    + " | " + r.getDistance()
                    + " | " + r.getSummary());
        }

        for (RouteOption r : allRoutes) {
            if (r.getMode().equals(travelMode)) {
                filtered.add(r);
            } else if (r.getMode().equals("user")) {
                String communityMode = r.getCommunityMode();

                if (communityMode == null || communityMode.trim().isEmpty()) {
                    communityMode = "walking";
                }

                if (communityMode.equals(travelMode)) {
                    filtered.add(r);
                }
            }
        }

        // Sécurité: nếu mode đang chọn không có route,
        // nhưng Google có route mode khác thì vẫn hiển thị thay vì báo rỗng.
        if (filtered.isEmpty() && !allRoutes.isEmpty()) {
            Log.d(TAG, "Aucune route pour le mode sélectionné, fallback vers toutes les routes disponibles.");

            Toast.makeText(getContext(),
                    "Aucun trajet pour ce mode, affichage des autres options",
                    Toast.LENGTH_SHORT).show();

            filtered.addAll(allRoutes);
        }

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "Aucun trajet trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(filtered, (r1, r2) -> {
            if (r1.getMode().equals("user") && !r2.getMode().equals("user")) {
                return -1;
            }

            if (!r1.getMode().equals("user") && r2.getMode().equals("user")) {
                return 1;
            }

            return parseDuration(r1.getDuration()) - parseDuration(r2.getDuration());
        });

        if (filtered.size() > 5) {
            filtered = filtered.subList(0, 5);
        }

        adapter = new RouteOptionsAdapter(filtered, route -> {
            afficherRoute(route);

            if (route.getMode().equals("user")) {
                incrementUserRouteUsage(route);
            }
        });

        rvTousItineraires.setAdapter(adapter);

        afficherRoute(filtered.get(0));
    }

    private void afficherRoute(RouteOption route) {

        mMap.clear();
        if (route.getMode().equals("user")) {
            incrementUserRouteUsage(route);
        }

        int color;

        switch (route.getMode()) {
            case "transit":
                color = Color.parseColor("#4CAF50");
                break;
            case "walking":
                color = Color.parseColor("#FF9800");
                break;
            case "bicycling":
                color = Color.parseColor("#2196F3");
                break;
            case "driving":
                color = Color.parseColor("#F44336");
                break;
            case "user":
                color = Color.parseColor("#3FA34D");
                break;
            default:
                color = Color.BLUE;
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(route.getPolylinePoints())
                .width(12)
                .color(color);

        if (route.getMode().equals("user")) {
            polylineOptions.pattern(Arrays.asList(new Dash(25), new Gap(16)));
        }

        currentPolyline = mMap.addPolyline(polylineOptions);

        LatLng markerStart = route.getMode().equals("user")
                ? route.getPolylinePoints().get(0)
                : originLatLng;

        LatLng markerEnd = route.getMode().equals("user")
                ? route.getPolylinePoints().get(route.getPolylinePoints().size() - 1)
                : destLatLng;

        mMap.addMarker(new MarkerOptions().position(markerStart).title("Départ"));
        mMap.addMarker(new MarkerOptions().position(markerEnd).title("Arrivée"));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng p : route.getPolylinePoints()) {
            builder.include(p);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));

        Toast.makeText(getContext(),
                route.getMode() + " • " + route.getDuration(),
                Toast.LENGTH_SHORT).show();
    }

    private LatLng getLocationFromAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(requireContext());
            List<Address> list = geocoder.getFromLocationName(address, 1);

            if (!list.isEmpty()) {
                Address loc = list.get(0);
                return new LatLng(loc.getLatitude(), loc.getLongitude());
            }
        } catch (Exception ignored) {}

        return null;
    }

    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, lat = 0, lng = 0;

        while (index < encoded.length()) {

            int b, shift = 0, result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lat += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            lng += ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
    }
    @SuppressWarnings("unchecked")
    private void loadUserSuggestedRoutes() {
        if (originLatLng == null || destLatLng == null) {
            userRoutesLoaded = true;
            tryFinishLoadingRoutes();
            return;
        }

        db.collection("itineraires_utilisateurs")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                        Double startLat = document.getDouble("startLat");
                        Double startLng = document.getDouble("startLng");
                        Double endLat = document.getDouble("endLat");
                        Double endLng = document.getDouble("endLng");

                        if (startLat == null || startLng == null || endLat == null || endLng == null) {
                            continue;
                        }

                        LatLng routeStart = new LatLng(startLat, startLng);
                        LatLng routeEnd = new LatLng(endLat, endLng);

                        float distanceStart = distanceBetween(originLatLng, routeStart);
                        float distanceEnd = distanceBetween(destLatLng, routeEnd);

                        // MVP: route communauté nếu điểm đầu/cuối gần điểm user tìm
                        boolean isNearSearch =
                                distanceStart <= 700 &&
                                        distanceEnd <= 700;

                        if (!isNearSearch) {
                            continue;
                        }

                        List<Map<String, Object>> pointsFirebase =
                                (List<Map<String, Object>>) document.get("points");

                        if (pointsFirebase == null || pointsFirebase.size() < 2) {
                            continue;
                        }

                        List<LatLng> points = new ArrayList<>();

                        for (Map<String, Object> pointMap : pointsFirebase) {
                            Object latObj = pointMap.get("lat");
                            Object lngObj = pointMap.get("lng");

                            if (latObj instanceof Number && lngObj instanceof Number) {
                                double lat = ((Number) latObj).doubleValue();
                                double lng = ((Number) lngObj).doubleValue();

                                points.add(new LatLng(lat, lng));
                            }
                        }

                        if (points.size() < 2) {
                            continue;
                        }

                        String title = document.getString("title");
                        String duration = document.getString("duration");
                        String distance = document.getString("distance");

                        if (title == null || title.trim().isEmpty()) {
                            title = "Proposition utilisateur";
                        }

                        if (duration == null || duration.trim().isEmpty()) {
                            duration = "Durée inconnue";
                        }

                        if (distance == null || distance.trim().isEmpty()) {
                            distance = calculateDistanceText(points);
                        }

                        String communityMode = document.getString("mode");

                        if (communityMode == null || communityMode.trim().isEmpty()) {
                            communityMode = "walking";
                        }

                        RouteOption userRoute = new RouteOption(
                                "user",
                                duration,
                                distance,
                                title,
                                points
                        );

                        userRoute.setDocumentId(document.getId());
                        userRoute.setCommunityMode(communityMode);

                        synchronized (allRoutes) {
                            allRoutes.add(userRoute);
                        }

                        Log.d(TAG, "Route communauté ajoutée : " + title);
                    }

                    userRoutesLoaded = true;
                    tryFinishLoadingRoutes();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur chargement itinéraires utilisateurs", e);

                    userRoutesLoaded = true;
                    tryFinishLoadingRoutes();
                });
    }
    private float distanceBetween(LatLng p1, LatLng p2) {
        float[] results = new float[1];

        android.location.Location.distanceBetween(
                p1.latitude,
                p1.longitude,
                p2.latitude,
                p2.longitude,
                results
        );

        return results[0];
    }
    private String calculateDistanceText(List<LatLng> points) {
        if (points == null || points.size() < 2) {
            return "0 m";
        }

        float total = 0;

        for (int i = 1; i < points.size(); i++) {
            total += distanceBetween(points.get(i - 1), points.get(i));
        }

        if (total < 1000) {
            return Math.round(total) + " m";
        }

        return String.format(Locale.FRANCE, "%.1f km", total / 1000);
    }
    private void incrementUserRouteUsage(RouteOption route) {
        if (route.getDocumentId() == null || route.getDocumentId().trim().isEmpty()) {
            return;
        }

        db.collection("itineraires_utilisateurs")
                .document(route.getDocumentId())
                .update("usageCount", FieldValue.increment(1))
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "usageCount augmenté pour route communauté")
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur update usageCount", e)
                );
    }
}
// Classe utilitaire pour éviter de réécrire les méthodes inutiles
abstract class SimpleTextWatcher implements android.text.TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(android.text.Editable s) {}

}