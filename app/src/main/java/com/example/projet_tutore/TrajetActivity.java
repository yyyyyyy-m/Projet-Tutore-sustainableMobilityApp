package com.example.projet_tutore;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class TrajetActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BottomSheetBehavior<View> behavior;

    private EditText etDepart, etArrivee;
    private Button btnCalculer, btnConnexion, btnOpenSearch;

    // ⚠️ METS TA VRAIE CLÉ API ICI
    private final String API_KEY = "TA_CLE_API";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajet);

        // INIT VUES
        etDepart = findViewById(R.id.etDepart);
        etArrivee = findViewById(R.id.etArrivee);
        btnCalculer = findViewById(R.id.btnCalculerTrajet);
        btnConnexion = findViewById(R.id.btnConnexion);
        btnOpenSearch = findViewById(R.id.btnOpenSearch);

        // BOTTOM SHEET
        View bottomSheet = findViewById(R.id.bottomSheet);
        behavior = BottomSheetBehavior.from(bottomSheet);

        behavior.setPeekHeight(150);
        behavior.setFitToContents(true); // IMPORTANT
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // 🔍 BOUTON RECHERCHER
        btnOpenSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Recherche ouverte", Toast.LENGTH_SHORT).show();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // 🔐 BOUTON CONNEXION
        btnConnexion.setOnClickListener(v -> {
            Toast.makeText(this, "Connexion cliquée", Toast.LENGTH_SHORT).show();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // 📍 CALCUL TRAJET
        btnCalculer.setOnClickListener(v -> calculerTrajet());

        // MAP
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

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
    }

    // ================= TRAJET =================

    private void calculerTrajet() {

        String depart = etDepart.getText().toString().trim();
        String arrivee = etArrivee.getText().toString().trim();

        if (depart.isEmpty() || arrivee.isEmpty()) {
            Toast.makeText(this, "Remplis les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = getLocationFromAddress(depart);
        LatLng dest = getLocationFromAddress(arrivee);

        if (origin == null || dest == null) {
            Toast.makeText(this, "Adresse invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        drawRoute(origin, dest);
    }

    private LatLng getLocationFromAddress(String address) {
        try {
            Geocoder geocoder = new Geocoder(this);
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
                + "&mode=driving"
                + "&key=" + API_KEY;

        Log.d("API_URL", url);

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

                if (!data.getString("status").equals("OK")) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erreur API", Toast.LENGTH_SHORT).show());
                    return;
                }

                JSONArray routes = data.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);

                JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

                String distance = leg.getJSONObject("distance").getString("text");
                String duration = leg.getJSONObject("duration").getString("text");

                String polyline = route
                        .getJSONObject("overview_polyline")
                        .getString("points");

                List<LatLng> points = decodePolyline(polyline);

                runOnUiThread(() -> {

                    mMap.clear();

                    mMap.addPolyline(new PolylineOptions()
                            .addAll(points)
                            .width(12)
                            .color(Color.BLUE));

                    mMap.addMarker(new MarkerOptions().position(origin).title("Départ"));
                    mMap.addMarker(new MarkerOptions().position(dest).title("Arrivée"));

                    Toast.makeText(this,
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
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
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