package com.example.projet_tutore;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DrivingRouteFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView tvRouteTitle;
    private TextView tvRouteInfo;
    private TextView btnSearchRoute;
    private TextView btnConfirmRoute;
    private TextView btnBack;

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String originText = "";
    private String destinationText = "";

    private double originLat = Double.NaN;
    private double originLng = Double.NaN;
    private double destinationLat = Double.NaN;
    private double destinationLng = Double.NaN;

    private String lastPolyline = "";
    private String lastDistance = "";
    private String lastDuration = "";
    private LatLng lastOrigin;
    private LatLng lastDestination;

    public DrivingRouteFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driving_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRouteTitle = view.findViewById(R.id.tvRouteTitle);
        tvRouteInfo = view.findViewById(R.id.tvRouteInfo);
        btnSearchRoute = view.findViewById(R.id.btnSearchRoute);
        btnConfirmRoute = view.findViewById(R.id.btnConfirmRoute);
        btnBack = view.findViewById(R.id.btnBack);

        readArguments();

        tvRouteTitle.setText(originText + " → " + destinationText);

        btnBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnSearchRoute.setOnClickListener(v -> searchDrivingRoute());

        btnConfirmRoute.setOnClickListener(v -> confirmRoute());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.drivingRouteMap);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void readArguments() {
        Bundle args = getArguments();

        if (args == null) return;

        originText = args.getString("origin", "");
        destinationText = args.getString("destination", "");

        originLat = args.getDouble("originLat", Double.NaN);
        originLng = args.getDouble("originLng", Double.NaN);
        destinationLat = args.getDouble("destinationLat", Double.NaN);
        destinationLng = args.getDouble("destinationLng", Double.NaN);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng paris = new LatLng(48.8566, 2.3522);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 11f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (hasValidCoordinates(originLat, originLng) && hasValidCoordinates(destinationLat, destinationLng)) {
            LatLng origin = new LatLng(originLat, originLng);
            LatLng destination = new LatLng(destinationLat, destinationLng);

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(origin).title("Départ"));
            mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin);
            builder.include(destination);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 120));

            searchDrivingRoute();
        }
    }

    private void searchDrivingRoute() {
        if (!hasValidCoordinates(originLat, originLng) || !hasValidCoordinates(destinationLat, destinationLng)) {
            Toast.makeText(requireContext(), "Coordonnées manquantes", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = new LatLng(originLat, originLng);
        LatLng destination = new LatLng(destinationLat, destinationLng);

        requestDirections(origin, destination);
    }

    private void requestDirections(LatLng origin, LatLng destination) {
        String apiKey = getMapsApiKey();

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("DEFAULT_API_KEY")) {
            Toast.makeText(requireContext(), "Clé Google Maps API manquante", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String originParam = origin.latitude + "," + origin.longitude;
            String destinationParam = destination.latitude + "," + destination.longitude;

            String url = "https://maps.googleapis.com/maps/api/directions/json?"
                    + "origin=" + URLEncoder.encode(originParam, "UTF-8")
                    + "&destination=" + URLEncoder.encode(destinationParam, "UTF-8")
                    + "&mode=driving"
                    + "&alternatives=false"
                    + "&key=" + apiKey;

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            tvRouteInfo.setText("Recherche de l’itinéraire en voiture...");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                    showRouteError("Erreur réseau");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (response.body() == null) {
                            showRouteError("Réponse vide");
                            return;
                        }

                        String json = response.body().string();
                        parseAndDisplayRoute(json, origin, destination);

                    } catch (Exception e) {
                        showRouteError("Erreur lors du traitement de l’itinéraire");
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erreur de requête", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseAndDisplayRoute(String json, LatLng origin, LatLng destination) {
        try {
            JSONObject root = new JSONObject(json);
            String status = root.optString("status");

            if (!"OK".equals(status)) {
                showRouteError("Aucun itinéraire trouvé : " + status);
                return;
            }

            JSONArray routes = root.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);

            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPolyline = overviewPolyline.getString("points");

            JSONArray legs = route.getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);

            String distanceText = leg.getJSONObject("distance").getString("text");
            String durationText = leg.getJSONObject("duration").getString("text");

            lastPolyline = encodedPolyline;
            lastDistance = distanceText;
            lastDuration = durationText;
            lastOrigin = origin;
            lastDestination = destination;

            List<LatLng> routePoints = decodePolyline(encodedPolyline);

            mainHandler.post(() -> drawRoute(origin, destination, routePoints, distanceText, durationText));

        } catch (Exception e) {
            showRouteError("Impossible de lire l’itinéraire");
        }
    }

    private void drawRoute(LatLng origin, LatLng destination, List<LatLng> routePoints,
                           String distanceText, String durationText) {
        if (mMap == null) return;

        mMap.clear();

        mMap.addMarker(new MarkerOptions()
                .position(origin)
                .title("Départ"));

        mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title("Destination"));

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(12f);

        mMap.addPolyline(polylineOptions);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(origin);
        boundsBuilder.include(destination);

        for (LatLng point : routePoints) {
            boundsBuilder.include(point);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));

        tvRouteInfo.setText("Distance : " + distanceText + "\nDurée estimée : " + durationText);
    }

    private void confirmRoute() {
        if (lastPolyline == null || lastPolyline.isEmpty()
                || lastOrigin == null || lastDestination == null) {
            Toast.makeText(requireContext(),
                    "Veuillez d'abord calculer un itinéraire",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle result = new Bundle();
        result.putString("routePolyline", lastPolyline);
        result.putString("routeDistance", lastDistance);
        result.putString("routeDuration", lastDuration);

        result.putDouble("routeOriginLat", lastOrigin.latitude);
        result.putDouble("routeOriginLng", lastOrigin.longitude);
        result.putDouble("routeDestinationLat", lastDestination.latitude);
        result.putDouble("routeDestinationLng", lastDestination.longitude);

        getParentFragmentManager().setFragmentResult("driving_route_result", result);

        Navigation.findNavController(requireView()).popBackStack();
    }

    private void showRouteError(String message) {
        mainHandler.post(() -> {
            tvRouteInfo.setText(message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private String getMapsApiKey() {
        try {
            ApplicationInfo appInfo = requireContext()
                    .getPackageManager()
                    .getApplicationInfo(requireContext().getPackageName(),
                            android.content.pm.PackageManager.GET_META_DATA);

            return appInfo.metaData.getString("com.google.android.geo.API_KEY");

        } catch (Exception e) {
            return "";
        }
    }

    private boolean hasValidCoordinates(double lat, double lng) {
        return !Double.isNaN(lat) && !Double.isNaN(lng);
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;

            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            LatLng point = new LatLng(lat / 1E5, lng / 1E5);
            poly.add(point);
        }

        return poly;
    }
}