package com.example.projet_tutore;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapPickerFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText etSearchAddress;
    private TextView btnConfirmLocation;
    private TextView tvSelectedAddress;

    private String target = "";
    private String selectedAddress = "";
    private LatLng selectedLatLng;

    public MapPickerFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearchAddress = view.findViewById(R.id.etSearchAddress);
        TextView btnSearchAddress = view.findViewById(R.id.btnSearchAddress);
        btnConfirmLocation = view.findViewById(R.id.btnConfirmLocation);
        tvSelectedAddress = view.findViewById(R.id.tvSelectedAddress);
        TextView btnBack = view.findViewById(R.id.btnBack);

        Bundle args = getArguments();
        if (args != null) {
            target = args.getString("target", "");
            String initialAddress = args.getString("initialAddress", "");
            etSearchAddress.setText(initialAddress);
        }

        btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnSearchAddress.setOnClickListener(v -> searchAddress());

        btnConfirmLocation.setOnClickListener(v -> confirmLocation());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapPickerMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng paris = new LatLng(48.8566, 2.3522);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 11f));

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            selectedAddress = getAddressFromLatLng(latLng);

            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Lieu sélectionné"));

            tvSelectedAddress.setText(selectedAddress);
        });
    }

    private void searchAddress() {
        String query = etSearchAddress.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez saisir une adresse", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.FRANCE);
            List<Address> results = geocoder.getFromLocationName(query, 1);

            if (results == null || results.isEmpty()) {
                Toast.makeText(requireContext(), "Adresse introuvable", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = results.get(0);
            selectedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
            selectedAddress = formatAddress(address);

            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(selectedLatLng)
                    .title("Lieu sélectionné"));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
            tvSelectedAddress.setText(selectedAddress);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erreur de recherche d’adresse", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLocation() {
        if (selectedLatLng == null || selectedAddress.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez choisir un lieu sur la carte", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle result = new Bundle();
        result.putString("target", target);
        result.putString("address", selectedAddress);
        result.putDouble("lat", selectedLatLng.latitude);
        result.putDouble("lng", selectedLatLng.longitude);

        getParentFragmentManager().setFragmentResult("map_picker_result", result);

        Navigation.findNavController(requireView()).popBackStack();
    }

    private String getAddressFromLatLng(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.FRANCE);
            List<Address> results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (results != null && !results.isEmpty()) {
                return formatAddress(results.get(0));
            }
        } catch (Exception ignored) {}

        return latLng.latitude + ", " + latLng.longitude;
    }

    private String formatAddress(Address address) {
        String line = address.getAddressLine(0);
        return line != null ? line : "Adresse sélectionnée";
    }
}