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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "CARTE_DEBUG";
    private FusedLocationProviderClient fusedLocationClient;
    private BottomSheetBehavior<View> behavior;

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
        destLatLng = getLocationFromAddress(arrivee);

        if (originLatLng == null || destLatLng == null) {
            Toast.makeText(getContext(), "Adresse invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        completedRequests = 0;
        allRoutes.clear();

        String[] modes = {"transit", "driving", "walking", "bicycling"};

        for (String mode : modes) {
            fetchRoute(mode);
            Log.d(TAG, "Départ: " + depart);
            Log.d(TAG, "Arrivée: " + arrivee);
            Log.d(TAG, "Origin: " + originLatLng);
            Log.d(TAG, "Destination: " + destLatLng);
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

                    if (completedRequests == 4) {
                        Log.d(TAG, "TOTAL ROUTES RECUPEREES: " + allRoutes.size());
                        filtrerRoutes();
                    }
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

        for (RouteOption r : allRoutes) {
            if (r.getMode().equals(travelMode)) {
                filtered.add(r);
            }
        }

        if (filtered.isEmpty()) {
            Toast.makeText(getContext(), "Aucun trajet trouvé", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.sort(filtered, (r1, r2) ->
                parseDuration(r1.getDuration()) - parseDuration(r2.getDuration())
        );

        if (filtered.size() > 5) {
            filtered = filtered.subList(0, 5);
        }

        adapter = new RouteOptionsAdapter(filtered, this::afficherRoute);
        rvTousItineraires.setAdapter(adapter);


        afficherRoute(filtered.get(0));
    }

    private void afficherRoute(RouteOption route) {

        mMap.clear();

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
            default:
                color = Color.BLUE;
        }

        currentPolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(route.getPolylinePoints())
                .width(12)
                .color(color));

        mMap.addMarker(new MarkerOptions().position(originLatLng).title("Départ"));
        mMap.addMarker(new MarkerOptions().position(destLatLng).title("Arrivée"));

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
}