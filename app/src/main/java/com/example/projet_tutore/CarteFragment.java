package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.projet_tutore.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class CarteFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private BottomSheetBehavior<View> behavior;
    private Button btnOpenSearch;
    private Button btnConnexion;

    public CarteFragment() {}

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

        // Initialisation des vues
        btnConnexion = v.findViewById(R.id.btnConnexion);
        btnOpenSearch = v.findViewById(R.id.btnOpenSearch);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Configuration du Bottom Sheet
        View bottomSheet = v.findViewById(R.id.bottomSheet);
        if (bottomSheet != null) {
            behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setPeekHeight(150);
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        // Bouton Connexion (pour l'instant toast)
        btnConnexion.setOnClickListener(view ->
                Toast.makeText(requireContext(),
                        "Fonctionnalité de connexion à implémenter",
                        Toast.LENGTH_SHORT).show());
        btnOpenSearch.setOnClickListener(ve -> {
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        });


        // Initialisation de la carte (IMPORTANT: getChildFragmentManager)
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(requireContext(),
                    "Erreur: Fragment carte non trouvé",
                    Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(),
                    "Erreur lors du chargement de la carte: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
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
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), (Location location) -> {
                    if (location != null && mMap != null) {
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
                .addOnFailureListener(requireActivity(), e -> {
                    Toast.makeText(requireContext(),
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
                Toast.makeText(requireContext(),
                        "Permission de localisation refusée",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}