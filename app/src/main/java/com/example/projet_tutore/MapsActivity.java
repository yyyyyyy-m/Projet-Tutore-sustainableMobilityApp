package com.example.projet_tutore;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajet);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng orsay = new LatLng(48.6985, 2.2137);
        LatLng massy = new LatLng(48.8075, 2.2674);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(orsay, 12f));

        mMap.addMarker(new MarkerOptions()
                .position(orsay)
                .title("Départ"));

        mMap.addMarker(new MarkerOptions()
                .position(massy)
                .title("Arrivée"));

    }

    public String getAddressFromLatLng(Context context, double lat, double lng) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                return address.getAddressLine(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Adresse inconnue";
    }


}

