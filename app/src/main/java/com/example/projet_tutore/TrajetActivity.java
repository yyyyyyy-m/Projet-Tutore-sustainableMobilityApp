package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TrajetActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private BottomSheetBehavior<View> behavior;
    private Button btnConnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajet);

        // Initialisation des vues
        btnConnexion = findViewById(R.id.btnConnexion);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configuration du Bottom Sheet
        View bottomSheet = findViewById(R.id.bottomSheet);
        if (bottomSheet != null) {
            behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(150);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        // Configuration du bouton Connexion
        btnConnexion.setOnClickListener(v -> {
            Toast.makeText(this, "Fonctionnalité de connexion à implémenter", Toast.LENGTH_SHORT).show();
        });

        // Initialisation de la carte
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Erreur: Fragment carte non trouvé", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Activer les contrôles de zoom
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);

            // Position par défaut (Tunis)
            LatLng defaultLocation = new LatLng(36.8065, 10.1815);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

            mMap.addMarker(new MarkerOptions()
                    .position(defaultLocation)
                    .title("Tunis")
                    .snippet("Capitale de la Tunisie"));

            // Vérifier la permission de localisation
            checkLocationPermission();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du chargement de la carte: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        mMap.clear();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));

                        mMap.addMarker(new MarkerOptions()
                                .position(currentLatLng)
                                .title("Ma position")
                                .snippet("Vous êtes ici"));
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this,
                            "Impossible d'obtenir la position: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocation();
            } else {
                Toast.makeText(this,
                        "Permission de localisation refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}