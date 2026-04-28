package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
    private FusedLocationProviderClient fusedLocationClient;

    private BottomSheetBehavior<View> behavior;

    private Button btnOpenSearch, btnConnexion, btnCalculer;
    private ImageButton bus ;
    private EditText etDepart, etArrivee;
    private String travelMode = "driving";

    private static final int LOCATION_PERMISSION_REQUEST = 1;

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
        super.onViewCreated(v, savedInstanceState);

        btnConnexion = v.findViewById(R.id.btnConnexion);
        btnOpenSearch = v.findViewById(R.id.btnOpenSearch);
        btnCalculer = v.findViewById(R.id.btnCalculerTrajet);

        etDepart = v.findViewById(R.id.etDepart);
        etArrivee = v.findViewById(R.id.etArrivee);

        bus = v.findViewById(R.id.btnModeBus);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        View bottomSheet = v.findViewById(R.id.bottomSheet);

        if (bottomSheet != null) {
            behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(150);
            behavior.setFitToContents(true);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        btnOpenSearch.setOnClickListener(view ->
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        );

        btnConnexion.setOnClickListener(view ->
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        );

        btnCalculer.setOnClickListener(view -> calculerTrajet());

        v.findViewById(R.id.btnModeBus).setOnClickListener(ve -> travelMode = "transit");

        v.findViewById(R.id.btnModeMarche).setOnClickListener(vi -> travelMode = "walking");

        v.findViewById(R.id.btnModeVelo).setOnClickListener(vo -> travelMode = "bicycling");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // ================= MAP =================

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng tunis = new LatLng(36.8065, 10.1815);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tunis, 12f));

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            getCurrentLocation();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
        }
    }

    private void enableMyLocation() {
        if (mMap == null) return;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                        mMap.addMarker(new MarkerOptions().position(pos).title("Ma position"));
                    }
                });
    }

    // ================= TRAJET =================

    private void calculerTrajet() {

        String depart = etDepart.getText().toString().trim();
        String arrivee = etArrivee.getText().toString().trim();

        if (depart.isEmpty() || arrivee.isEmpty()) {
            Toast.makeText(requireContext(), "Remplis les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = getLocationFromAddress(depart);
        LatLng dest = getLocationFromAddress(arrivee);

        if (origin == null || dest == null) {
            Toast.makeText(requireContext(), "Adresse invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        drawRoute(origin, dest);
    }

    private LatLng getLocationFromAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(requireContext());
            List<Address> list = geocoder.getFromLocationName(address, 1);

            if (list != null && !list.isEmpty()) {
                Address loc = list.get(0);
                return new LatLng(loc.getLatitude(), loc.getLongitude());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void drawRoute(LatLng origin, LatLng dest) {

        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + dest.latitude + "," + dest.longitude
                + "&mode=" + travelMode
                + (travelMode.equals("transit") ? "&departure_time=now" : "")
                + "&key=" + API_KEY;

        new Thread(() -> {
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.connect();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                StringBuilder json = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONObject data = new JSONObject(json.toString());

                if (!data.getString("status").equals("OK")) return;

                JSONObject route = data.getJSONArray("routes").getJSONObject(0);
                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

                String distance = leg.getJSONObject("distance").getString("text");
                String duration = leg.getJSONObject("duration").getString("text");

                String polyline = route.getJSONObject("overview_polyline").getString("points");

                List<LatLng> points = decodePolyline(polyline);

                requireActivity().runOnUiThread(() -> {

                    mMap.clear();

                    mMap.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .width(10)
                            .color(Color.BLUE));

                    mMap.addMarker(new MarkerOptions().position(origin).title("Départ"));
                    mMap.addMarker(new MarkerOptions().position(dest).title("Arrivée"));
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(origin);
                    builder.include(dest);

                    LatLngBounds bounds = builder.build();

                    int padding = 150;

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                    Toast.makeText(requireContext(),
                            "Distance: " + distance + " | Durée: " + duration,
                            Toast.LENGTH_LONG).show();


                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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