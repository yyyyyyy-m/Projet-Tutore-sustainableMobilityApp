package com.example.projet_tutore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.widget.ImageButton;

public class ItinerairesUtilisateursFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private ImageButton btnPauseRecord;
    private TextView btnCancelRecord;
    private TextView btnSendRoute;

    private TextView tvRecordTimer;
    private TextView tvDistanceMeters;
    private TextView tvGpsPoints;
    private TextView tvEcoPoints;

    private boolean hasStarted = false;
    private boolean isRecording = false;

    private List<LatLng> recordedPoints = new ArrayList<>();
    private Polyline recordedPolyline;
    private LocationCallback recordingLocationCallback;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTimeMillis = 0;
    private long elapsedBeforePause = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long elapsed = elapsedBeforePause;

            if (isRecording) {
                elapsed += System.currentTimeMillis() - startTimeMillis;
            }

            tvRecordTimer.setText(formatElapsedTime(elapsed));
            timerHandler.postDelayed(this, 1000);
        }
    };

    public ItinerairesUtilisateursFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_itineraires_utilisateurs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupActions();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.mapSharedRoutes);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews(View view) {
        btnPauseRecord = view.findViewById(R.id.btnPauseRecord);
        btnCancelRecord = view.findViewById(R.id.btnCancelRecord);
        btnSendRoute = view.findViewById(R.id.btnSendRoute);

        tvRecordTimer = view.findViewById(R.id.tvRecordTimer);
        tvDistanceMeters = view.findViewById(R.id.tvDistanceMeters);
        tvGpsPoints = view.findViewById(R.id.tvGpsPoints);
        tvEcoPoints = view.findViewById(R.id.tvEcoPoints);

        // Au début, le bouton sert à démarrer l'enregistrement.
        btnPauseRecord.setImageResource(R.drawable.ic_play_tilt_black);
        btnSendRoute.setAlpha(0.6f);
    }

    private void setupActions() {
        btnPauseRecord.setOnClickListener(v -> {
            if (!hasStarted) {
                startRecordingRoute();
            } else if (isRecording) {
                pauseRecordingRoute();
            } else {
                resumeRecordingRoute();
            }
        });

        btnCancelRecord.setOnClickListener(v -> cancelRecording());

        btnSendRoute.setOnClickListener(v -> showPublishDialog());
    }
    private void showPublishDialog() {
        if (recordedPoints.size() < 2) {
            Toast.makeText(requireContext(),
                    "Itinéraire trop court pour être publié",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRecording) {
            pauseRecordingRoute();
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_publish_itineraire, null);

        EditText etRouteTitle = dialogView.findViewById(R.id.etRouteTitle);
        EditText etRouteDepart = dialogView.findViewById(R.id.etRouteDepart);
        EditText etRouteDestination = dialogView.findViewById(R.id.etRouteDestination);
        EditText etRouteDescription = dialogView.findViewById(R.id.etRouteDescription);

        TextView btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        TextView btnDialogPublish = dialogView.findViewById(R.id.btnDialogPublish);

        etRouteTitle.setText("Itinéraire à pied partagé");
        etRouteDepart.setText("Départ enregistré");
        etRouteDestination.setText("Arrivée enregistrée");
        etRouteDescription.setText("Itinéraire enregistré avec le GPS.");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());

        btnDialogPublish.setOnClickListener(v -> {
            String title = etRouteTitle.getText().toString().trim();
            String depart = etRouteDepart.getText().toString().trim();
            String destination = etRouteDestination.getText().toString().trim();
            String description = etRouteDescription.getText().toString().trim();

            if (title.isEmpty() || depart.isEmpty() || destination.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Titre, départ et arrivée sont obligatoires",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                description = "Itinéraire enregistré par un utilisateur avec le GPS.";
            }

            dialog.dismiss();
            publishRecordedRoute(title, depart, destination, description);
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNav);

        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.carteFragment).setChecked(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isRecording) {
            pauseRecordingRoute();
        }

        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        showCurrentLocationOnMap();
    }

    private void showCurrentLocationOnMap() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentPosition = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        mMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(currentPosition, 16f)
                        );

                    } else {
                        Toast.makeText(requireContext(),
                                "Position indisponible pour le moment",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startRecordingRoute() {
        if (mMap == null) {
            Toast.makeText(requireContext(), "Carte pas encore prête", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            return;
        }

        hasStarted = true;
        isRecording = true;
        recordedPoints.clear();
        elapsedBeforePause = 0;
        startTimeMillis = System.currentTimeMillis();

        btnPauseRecord.setImageResource(R.drawable.ic_pause_black);
        btnSendRoute.setAlpha(1f);

        if (recordedPolyline != null) {
            recordedPolyline.remove();
            recordedPolyline = null;
        }

        mMap.clear();
        mMap.setMyLocationEnabled(true);

        // Ajoute rapidement la dernière position connue comme premier point.
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng firstPoint = new LatLng(location.getLatitude(), location.getLongitude());
                        recordedPoints.add(firstPoint);

                        mMap.addMarker(new MarkerOptions()
                                .position(firstPoint)
                                .title("Départ"));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 17f));
                        updateStats();
                    }
                });

        startTimer();
        startLocationUpdates();

        Toast.makeText(requireContext(), "Enregistrement commencé", Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        recordingLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!isRecording) return;

                for (Location location : locationResult.getLocations()) {
                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());

                    if (shouldAddPoint(newPoint)) {
                        recordedPoints.add(newPoint);
                        drawRecordedRoute();
                        updateStats();
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                recordingLocationCallback,
                Looper.getMainLooper()
        );
    }

    private boolean shouldAddPoint(LatLng newPoint) {
        if (recordedPoints.isEmpty()) {
            return true;
        }

        LatLng lastPoint = recordedPoints.get(recordedPoints.size() - 1);

        return distanceBetween(lastPoint, newPoint) >= 5;
    }

    private void pauseRecordingRoute() {
        if (!isRecording) return;

        isRecording = false;
        elapsedBeforePause += System.currentTimeMillis() - startTimeMillis;

        if (recordingLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(recordingLocationCallback);
        }

        btnPauseRecord.setImageResource(R.drawable.ic_play_tilt_black);

        Toast.makeText(requireContext(), "Enregistrement en pause", Toast.LENGTH_SHORT).show();
    }

    private void resumeRecordingRoute() {
        if (!hasStarted) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            return;
        }

        isRecording = true;
        startTimeMillis = System.currentTimeMillis();

        btnPauseRecord.setImageResource(R.drawable.ic_pause_black);

        startTimer();
        startLocationUpdates();

        Toast.makeText(requireContext(), "Enregistrement repris", Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.post(timerRunnable);
    }

    private void drawRecordedRoute() {
        if (mMap == null || recordedPoints.size() < 2) return;

        if (recordedPolyline != null) {
            recordedPolyline.remove();
        }

        recordedPolyline = mMap.addPolyline(new PolylineOptions()
                .addAll(recordedPoints)
                .width(10)
                .color(Color.parseColor("#3FA34D"))
                .pattern(Arrays.asList(new Dash(25), new Gap(16))));

        LatLng lastPoint = recordedPoints.get(recordedPoints.size() - 1);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 17f));
    }

    private void cancelRecording() {
        if (recordingLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(recordingLocationCallback);
        }

        timerHandler.removeCallbacks(timerRunnable);

        hasStarted = false;
        isRecording = false;
        elapsedBeforePause = 0;
        startTimeMillis = 0;

        recordedPoints.clear();

        if (recordedPolyline != null) {
            recordedPolyline.remove();
            recordedPolyline = null;
        }

        if (mMap != null) {
            mMap.clear();
            showCurrentLocationOnMap();
        }

        btnPauseRecord.setImageResource(R.drawable.ic_play_tilt_black);
        tvRecordTimer.setText("00:00");
        tvDistanceMeters.setText("0");
        tvGpsPoints.setText("0");
        tvEcoPoints.setText("0");
        btnSendRoute.setAlpha(0.6f);

        Toast.makeText(requireContext(), "Enregistrement annulé", Toast.LENGTH_SHORT).show();
    }

    private void publishRecordedRoute(String title, String depart, String destination, String description) {
        if (recordedPoints.size() < 2) {
            Toast.makeText(requireContext(),
                    "Itinéraire trop court pour être publié",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRecording) {
            pauseRecordingRoute();
        }

        String userId = "guest";

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        List<Map<String, Object>> points = new ArrayList<>();

        for (LatLng latLng : recordedPoints) {
            Map<String, Object> point = new HashMap<>();
            point.put("lat", latLng.latitude);
            point.put("lng", latLng.longitude);
            points.add(point);
        }

        String distanceText = calculateRecordedDistanceText();
        String durationText = tvRecordTimer.getText().toString();
        int ecoPoints = calculateEcoPoints();
        LatLng startPoint = recordedPoints.get(0);
        LatLng endPoint = recordedPoints.get(recordedPoints.size() - 1);

        Map<String, Object> itineraire = new HashMap<>();
        itineraire.put("title", title);
        itineraire.put("depart", depart);
        itineraire.put("destination", destination);
        itineraire.put("departLower", depart.toLowerCase(Locale.ROOT));
        itineraire.put("destinationLower", destination.toLowerCase(Locale.ROOT));
        itineraire.put("mode", "walking");
        itineraire.put("description", description);
        itineraire.put("distance", distanceText);
        itineraire.put("duration", durationText);
        itineraire.put("points", points);
        itineraire.put("startLat", startPoint.latitude);
        itineraire.put("startLng", startPoint.longitude);
        itineraire.put("endLat", endPoint.latitude);
        itineraire.put("endLng", endPoint.longitude);
        itineraire.put("likes", 0);
        itineraire.put("usageCount", 0);
        itineraire.put("status", "active");
        itineraire.put("ecoPoints", ecoPoints);
        itineraire.put("createdBy", userId);
        itineraire.put("createdAt", FieldValue.serverTimestamp());

        db.collection("itineraires_utilisateurs")
                .add(itineraire)
                .addOnSuccessListener(documentReference -> {
                    fitCameraToRecordedRoute();
                    showSuccessDialog();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erreur Firebase : " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void fitCameraToRecordedRoute() {
        if (mMap == null || recordedPoints.size() < 2) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (LatLng point : recordedPoints) {
            builder.include(point);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 140));
    }

    private void updateStats() {
        float totalDistance = calculateRecordedDistanceMeters();
        int roundedDistance = Math.round(totalDistance);
        int pointsCount = recordedPoints.size();
        int ecoPoints = calculateEcoPoints();

        tvDistanceMeters.setText(String.valueOf(roundedDistance));
        tvGpsPoints.setText(String.valueOf(pointsCount));
        tvEcoPoints.setText(String.valueOf(ecoPoints));
    }

    private int calculateEcoPoints() {
        float distance = calculateRecordedDistanceMeters();

        // 1 éco-point par 100 mètres, minimum 0.
        return Math.max(0, Math.round(distance / 100));
    }

    private String calculateRecordedDistanceText() {
        float totalDistance = calculateRecordedDistanceMeters();

        if (totalDistance < 1000) {
            return Math.round(totalDistance) + " m";
        }

        return String.format(Locale.FRANCE, "%.1f km", totalDistance / 1000);
    }

    private float calculateRecordedDistanceMeters() {
        if (recordedPoints.size() < 2) {
            return 0;
        }

        float totalDistance = 0;

        for (int i = 1; i < recordedPoints.size(); i++) {
            totalDistance += distanceBetween(recordedPoints.get(i - 1), recordedPoints.get(i));
        }

        return totalDistance;
    }

    private float distanceBetween(LatLng p1, LatLng p2) {
        float[] results = new float[1];

        Location.distanceBetween(
                p1.latitude,
                p1.longitude,
                p2.latitude,
                p2.longitude,
                results
        );

        return results[0];
    }

    private String formatElapsedTime(long elapsedMillis) {
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format(Locale.FRANCE, "%02d:%02d", minutes, seconds);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showCurrentLocationOnMap();
        } else {
            Toast.makeText(requireContext(),
                    "Permission de localisation refusée",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_success_itineraire, null);

        TextView btnSuccessOk = dialogView.findViewById(R.id.btnSuccessOk);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.setCanceledOnTouchOutside(false);

        btnSuccessOk.setOnClickListener(v -> {
            dialog.dismiss();
            resetAfterPublish();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.86);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    private void resetAfterPublish() {
        if (recordingLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(recordingLocationCallback);
        }

        timerHandler.removeCallbacks(timerRunnable);

        hasStarted = false;
        isRecording = false;
        elapsedBeforePause = 0;
        startTimeMillis = 0;

        recordedPoints.clear();

        if (recordedPolyline != null) {
            recordedPolyline.remove();
            recordedPolyline = null;
        }

        if (mMap != null) {
            mMap.clear();
            showCurrentLocationOnMap();
        }

        btnPauseRecord.setImageResource(R.drawable.ic_play_tilt_black);
        tvRecordTimer.setText("00:00");
        tvDistanceMeters.setText("0");
        tvGpsPoints.setText("0");
        tvEcoPoints.setText("0");
        btnSendRoute.setAlpha(0.6f);
    }
}