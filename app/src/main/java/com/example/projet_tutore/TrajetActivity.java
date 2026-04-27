package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class TrajetActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BottomSheetBehavior<View> behavior;
    private Button btnConnexion, btnOpenSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajet);

        btnConnexion = findViewById(R.id.btnConnexion);
        btnOpenSearch = findViewById(R.id.btnOpenSearch);

        View bottomSheet = findViewById(R.id.bottomSheet);

        behavior = BottomSheetBehavior.from(bottomSheet);

// IMPORTANT
        behavior.setPeekHeight(150); // hauteur fermée
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

// Permet d’ouvrir en plein écran
        behavior.setFitToContents(false);
        behavior.setHalfExpandedRatio(0.7f);

        btnOpenSearch.setOnClickListener(v -> {
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });

        btnConnexion.setOnClickListener(v -> {
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng tunis = new LatLng(36.8065, 10.1815);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tunis, 12f));
        mMap.addMarker(new MarkerOptions().position(tunis).title("Tunis"));
    }
}