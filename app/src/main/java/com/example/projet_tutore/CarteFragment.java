package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "CARTE_DEBUG";

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private BottomSheetBehavior<View> behavior;

    private Button btnCalculer;

    private AutoCompleteTextView etDepart;
    private AutoCompleteTextView etArrivee;

    private ImageButton btnModeBus;
    private ImageButton btnModeMarche;
    private ImageButton btnModeVelo;

    private RecyclerView rvTousItineraires;

    private RouteOptionsAdapter adapter;

    private final List<RouteOption> allRoutes = new ArrayList<>();

    private String travelMode = "transit";

    private LatLng originLatLng;
    private LatLng destLatLng;

    private Polyline currentPolyline;

    private int completedRequests = 0;

    private boolean userRoutesLoaded = false;

    private final String API_KEY = BuildConfig.MAPS_API_KEY;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(R.layout.fragment_carte, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View v,
            @Nullable Bundle savedInstanceState
    ) {

        db = FirebaseFirestore.getInstance();

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());

        btnCalculer = v.findViewById(R.id.btnCalculerTrajet);

        etDepart = v.findViewById(R.id.etDepart);
        etArrivee = v.findViewById(R.id.etArrivee);

        btnModeBus = v.findViewById(R.id.btnModeBus);
        btnModeMarche = v.findViewById(R.id.btnModeMarche);
        btnModeVelo = v.findViewById(R.id.btnModeVelo);

        rvTousItineraires = v.findViewById(R.id.rvTousItineraires);

        rvTousItineraires.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        View bottomSheet = v.findViewById(R.id.bottomSheet);

        behavior = BottomSheetBehavior.from(bottomSheet);

        setupAutocomplete();

        setupButtons();

        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupButtons() {

        btnCalculer.setOnClickListener(v -> calculerTousLesItineraires());

        btnModeBus.setOnClickListener(v -> {

            travelMode = "transit";

            updateButtons(btnModeBus);

            filtrerRoutes();
        });

        btnModeMarche.setOnClickListener(v -> {

            travelMode = "walking";

            updateButtons(btnModeMarche);

            filtrerRoutes();
        });

        btnModeVelo.setOnClickListener(v -> {

            travelMode = "bicycling";

            updateButtons(btnModeVelo);

            filtrerRoutes();
        });
    }

    private void setupAutocomplete() {

        ArrayAdapter<String> adapterDepart =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line
                );

        ArrayAdapter<String> adapterArrivee =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line
                );

        etDepart.setAdapter(adapterDepart);
        etArrivee.setAdapter(adapterArrivee);

        etDepart.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                if (s.length() >= 3) {

                    fetchAddressSuggestionsOSM(
                            s.toString(),
                            adapterDepart
                    );
                }
            }
        });

        etArrivee.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                if (s.length() >= 3) {

                    fetchAddressSuggestionsOSM(
                            s.toString(),
                            adapterArrivee
                    );
                }
            }
        });
    }

    private void updateButtons(ImageButton selected) {

        btnModeBus.setBackgroundColor(Color.WHITE);
        btnModeMarche.setBackgroundColor(Color.WHITE);
        btnModeVelo.setBackgroundColor(Color.WHITE);

        selected.setBackgroundColor(
                Color.parseColor("#B8E6C1")
        );
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        checkLocationPermission();
    }

    private void checkLocationPermission() {

        if (
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        ) {

            mMap.setMyLocationEnabled(true);

            getCurrentLocation();

        } else {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1
            );
        }
    }

    private void getCurrentLocation() {

        if (
                ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return;
        }

        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        originLatLng = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                        originLatLng,
                                        14
                                )
                        );

                        Log.d(
                                TAG,
                                "Position utilisateur récupérée"
                        );
                    }
                });
    }

    private void calculerTousLesItineraires() {

        String depart =
                etDepart.getText().toString().trim();

        String arrivee =
                etArrivee.getText().toString().trim();

        if (arrivee.isEmpty()) {

            Toast.makeText(
                    getContext(),
                    "Veuillez saisir une destination",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (depart.isEmpty()) {

            if (
                    ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                return;
            }

            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (location == null) {

                            Toast.makeText(
                                    getContext(),
                                    "Impossible de récupérer votre position",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        originLatLng = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        lancerCalcul(arrivee);
                    });

        } else {

            originLatLng =
                    getLocationFromAddress(depart);

            lancerCalcul(arrivee);
        }
    }

    private void lancerCalcul(String arrivee) {

        destLatLng =
                getLocationFromAddress(arrivee);

        if (originLatLng == null || destLatLng == null) {

            Toast.makeText(
                    getContext(),
                    "Adresse invalide",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        completedRequests = 0;

        userRoutesLoaded = false;

        allRoutes.clear();

        String[] modes = {
                "transit",
                "walking",
                "bicycling",
                "driving"
        };

        for (String mode : modes) {
            fetchRoute(mode);
        }

        loadUserSuggestedRoutes();
    }

    private void fetchRoute(String mode) {

        String url =
                "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin="
                        + originLatLng.latitude
                        + ","
                        + originLatLng.longitude
                        + "&destination="
                        + destLatLng.latitude
                        + ","
                        + destLatLng.longitude
                        + "&mode="
                        + mode
                        + "&alternatives=true"
                        + (mode.equals("transit")
                        ? "&departure_time=now"
                        : "")
                        + "&key="
                        + API_KEY;

        new Thread(() -> {

            try {

                Log.d(TAG, "URL = " + url);

                HttpURLConnection conn =
                        (HttpURLConnection)
                                new URL(url).openConnection();

                conn.connect();

                int responseCode =
                        conn.getResponseCode();

                Log.d(
                        TAG,
                        "HTTP CODE = " + responseCode
                );

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        conn.getInputStream()
                                )
                        );

                StringBuilder json =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONObject data =
                        new JSONObject(json.toString());

                String status =
                        data.getString("status");

                Log.d(TAG, "STATUS = " + status);

                if (!status.equals("OK")) {
                    return;
                }

                JSONArray routes =
                        data.getJSONArray("routes");

                for (int i = 0; i < routes.length(); i++) {

                    JSONObject route =
                            routes.getJSONObject(i);

                    JSONObject leg =
                            route.getJSONArray("legs")
                                    .getJSONObject(0);

                    String duration =
                            leg.getJSONObject("duration")
                                    .getString("text");

                    String distance =
                            leg.getJSONObject("distance")
                                    .getString("text");

                    String polyline =
                            route.getJSONObject(
                                            "overview_polyline"
                                    )
                                    .getString("points");

                    List<LatLng> points =
                            decodePolyline(polyline);

                    RouteOption option =
                            new RouteOption(
                                    mode,
                                    duration,
                                    distance,
                                    "",
                                    points
                            );

                    synchronized (allRoutes) {
                        allRoutes.add(option);
                    }
                }

            } catch (Exception e) {

                Log.e(TAG, "Erreur route", e);

            } finally {

                requireActivity().runOnUiThread(() -> {

                    completedRequests++;

                    tryFinishLoadingRoutes();
                });
            }

        }).start();
    }

    private void tryFinishLoadingRoutes() {

        Log.d(
                TAG,
                "completedRequests = "
                        + completedRequests
        );

        Log.d(
                TAG,
                "userRoutesLoaded = "
                        + userRoutesLoaded
        );

        Log.d(
                TAG,
                "allRoutes size = "
                        + allRoutes.size()
        );

        if (completedRequests == 4 && userRoutesLoaded) {

            filtrerRoutes();
        }
    }

    private void filtrerRoutes() {

        List<RouteOption> filtered =
                new ArrayList<>();

        for (RouteOption route : allRoutes) {

            if (route.getMode().equals("user")) {

                filtered.add(route);

            } else if (
                    route.getMode().equals(travelMode)
            ) {

                filtered.add(route);
            }
        }

        Log.d(
                TAG,
                "ROUTES FILTREES = "
                        + filtered.size()
        );

        if (filtered.isEmpty()) {

            Toast.makeText(
                    getContext(),
                    "Aucune route trouvée",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Collections.sort(
                filtered,
                (r1, r2) ->
                        parseDuration(r1.getDuration())
                                - parseDuration(r2.getDuration())
        );

        adapter =
                new RouteOptionsAdapter(
                        filtered,
                        this::afficherRoute
                );

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

            case "user":
                color = Color.parseColor("#8E24AA");
                break;

            default:
                color = Color.BLUE;
        }

        PolylineOptions options =
                new PolylineOptions()
                        .addAll(route.getPolylinePoints())
                        .width(12)
                        .color(color);

        if (route.getMode().equals("user")) {

            List<PatternItem> pattern =
                    new ArrayList<>();

            pattern.add(new Dash(30));
            pattern.add(new Gap(20));

            options.pattern(pattern);
        }

        currentPolyline =
                mMap.addPolyline(options);

        List<LatLng> points =
                route.getPolylinePoints();

        if (points.isEmpty()) {
            return;
        }

        mMap.addMarker(
                new MarkerOptions()
                        .position(points.get(0))
                        .title("Départ")
        );

        mMap.addMarker(
                new MarkerOptions()
                        .position(points.get(points.size() - 1))
                        .title("Arrivée")
        );

        LatLngBounds.Builder builder =
                new LatLngBounds.Builder();

        for (LatLng point : points) {
            builder.include(point);
        }

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                        builder.build(),
                        150
                )
        );
    }

    private void fetchAddressSuggestionsOSM(
            String query,
            ArrayAdapter<String> adapter
    ) {

        String url =
                "https://nominatim.openstreetmap.org/search?q="
                        + query.replace(" ", "%20")
                        + "&format=json&limit=10";

        new Thread(() -> {

            try {

                HttpURLConnection conn =
                        (HttpURLConnection)
                                new URL(url).openConnection();

                conn.setRequestProperty(
                        "User-Agent",
                        "Android-App"
                );

                conn.connect();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        conn.getInputStream()
                                )
                        );

                StringBuilder json =
                        new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONArray results =
                        new JSONArray(json.toString());

                List<String> suggestions =
                        new ArrayList<>();

                for (int i = 0; i < results.length(); i++) {

                    JSONObject obj =
                            results.getJSONObject(i);

                    suggestions.add(
                            obj.getString("display_name")
                    );
                }

                requireActivity().runOnUiThread(() -> {

                    adapter.clear();

                    adapter.addAll(suggestions);

                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {

                Log.e(TAG, "Erreur OSM", e);
            }

        }).start();
    }

    private LatLng getLocationFromAddress(String address) {

        try {

            Geocoder geocoder =
                    new Geocoder(requireContext());

            List<Address> addresses =
                    geocoder.getFromLocationName(
                            address,
                            1
                    );

            if (
                    addresses != null
                            && !addresses.isEmpty()
            ) {

                Address a = addresses.get(0);

                return new LatLng(
                        a.getLatitude(),
                        a.getLongitude()
                );
            }

        } catch (Exception e) {

            Log.e(TAG, "Erreur geocoder", e);
        }

        return null;
    }

    private int parseDuration(String duration) {

        try {

            int total = 0;

            if (duration.contains("hour")) {

                String[] parts =
                        duration.split("hour");

                total +=
                        Integer.parseInt(
                                parts[0].trim()
                        ) * 60;

                if (
                        parts.length > 1
                                && parts[1].contains("min")
                ) {

                    String mins =
                            parts[1].replaceAll(
                                    "[^0-9]",
                                    ""
                            );

                    if (!mins.isEmpty()) {
                        total += Integer.parseInt(mins);
                    }
                }

            } else if (duration.contains("min")) {

                total =
                        Integer.parseInt(
                                duration.replaceAll(
                                        "[^0-9]",
                                        ""
                                )
                        );
            }

            return total;

        } catch (Exception e) {

            return 9999;
        }
    }

    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<>();

        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < encoded.length()) {

            int b;
            int shift = 0;
            int result = 0;

            do {

                b = encoded.charAt(index++) - 63;

                result |= (b & 0x1f) << shift;

                shift += 5;

            } while (b >= 0x20);

            lat +=
                    ((result & 1) != 0
                            ? ~(result >> 1)
                            : (result >> 1));

            shift = 0;

            result = 0;

            do {

                b = encoded.charAt(index++) - 63;

                result |= (b & 0x1f) << shift;

                shift += 5;

            } while (b >= 0x20);

            lng +=
                    ((result & 1) != 0
                            ? ~(result >> 1)
                            : (result >> 1));

            poly.add(
                    new LatLng(
                            lat / 1E5,
                            lng / 1E5
                    )
            );
        }

        return poly;
    }

    private void loadUserSuggestedRoutes() {

        db.collection("itineraires_utilisateurs")
                .get()
                .addOnSuccessListener(documents -> {

                    for (DocumentSnapshot doc : documents) {

                        try {

                            List<Map<String, Object>> rawPoints =
                                    (List<Map<String, Object>>)
                                            doc.get("points");

                            if (
                                    rawPoints == null
                                            || rawPoints.size() < 2
                            ) {
                                continue;
                            }

                            List<LatLng> points =
                                    new ArrayList<>();

                            for (Map<String, Object> map : rawPoints) {

                                double lat =
                                        ((Number) map.get("lat"))
                                                .doubleValue();

                                double lng =
                                        ((Number) map.get("lng"))
                                                .doubleValue();

                                points.add(
                                        new LatLng(lat, lng)
                                );
                            }

                            RouteOption route =
                                    new RouteOption(
                                            "user",
                                            "Communauté",
                                            "0 km",
                                            "Itinéraire utilisateur",
                                            points
                                    );

                            allRoutes.add(route);

                        } catch (Exception e) {

                            Log.e(
                                    TAG,
                                    "Erreur route user",
                                    e
                            );
                        }
                    }

                    userRoutesLoaded = true;

                    tryFinishLoadingRoutes();
                })
                .addOnFailureListener(e -> {

                    userRoutesLoaded = true;

                    tryFinishLoadingRoutes();
                });
    }
}

abstract class SimpleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(
            CharSequence s,
            int start,
            int count,
            int after
    ) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}