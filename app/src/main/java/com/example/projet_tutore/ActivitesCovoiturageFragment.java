package com.example.projet_tutore;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ActivitesCovoiturageFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout activitiesContainer;
    private TextView tvLoading;

    public ActivitesCovoiturageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activites_covoiturage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        TextView btnClose = view.findViewById(R.id.btnClose);
        tvLoading = view.findViewById(R.id.tvLoading);
        activitiesContainer = view.findViewById(R.id.activitiesContainer);

        btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        loadUserActivities();
    }

    private void loadUserActivities() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            tvLoading.setVisibility(View.VISIBLE);
            tvLoading.setText("Veuillez vous connecter pour voir vos activités.");
            activitiesContainer.removeAllViews();
            return;
        }

        tvLoading.setVisibility(View.VISIBLE);
        tvLoading.setText("Chargement de vos activités...");
        activitiesContainer.removeAllViews();

        db.collection("reservations")
                .whereEqualTo("userId", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvLoading.setVisibility(View.GONE);
                    activitiesContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyMessage();
                        return;
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        addActivityCard(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    tvLoading.setVisibility(View.VISIBLE);
                    tvLoading.setText("Impossible de charger vos activités.");
                    Toast.makeText(requireContext(),
                            "Erreur Firebase : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void addActivityCard(DocumentSnapshot doc) {
        String reservationId = doc.getId();

        String depart = getStringValue(doc, "depart", "Départ inconnu");
        String arrivee = getStringValue(doc, "arrivee", "Arrivée inconnue");
        String date = getStringValue(doc, "date", "Date inconnue");
        String heure = getStringValue(doc, "heure", "");
        String status = getStringValue(doc, "status", "confirmed");
        String trajetId = getStringValue(doc, "trajetId", "");

        long placesReservees = getLongValue(doc, "placesReservees", 1);
        double prix = getDoubleValue(doc, "prix", 0.0);

        CardView card = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(16));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(18));
        card.setCardElevation(dpToPx(3));
        card.setUseCompatPadding(true);

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dpToPx(18), dpToPx(16), dpToPx(18), dpToPx(16));

        TextView title = new TextView(requireContext());
        title.setText(depart + " → " + arrivee);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF1F2937);
        content.addView(title);

        TextView dateView = new TextView(requireContext());
        dateView.setText("Date : " + date + (heure.isEmpty() ? "" : " à " + heure));
        dateView.setTextSize(14);
        dateView.setTextColor(0xFF555555);
        dateView.setPadding(0, dpToPx(8), 0, 0);
        content.addView(dateView);

        TextView placesView = new TextView(requireContext());
        placesView.setText("Places réservées : " + placesReservees);
        placesView.setTextSize(14);
        placesView.setTextColor(0xFF555555);
        placesView.setPadding(0, dpToPx(4), 0, 0);
        content.addView(placesView);

        TextView priceView = new TextView(requireContext());
        priceView.setText("Prix : " + prix + " €");
        priceView.setTextSize(14);
        priceView.setTextColor(0xFF555555);
        priceView.setPadding(0, dpToPx(4), 0, 0);
        content.addView(priceView);

        TextView statusView = new TextView(requireContext());
        statusView.setText("Statut : " + getStatusLabel(status));
        statusView.setTextSize(14);
        statusView.setTypeface(Typeface.DEFAULT_BOLD);
        statusView.setTextColor("cancelled".equals(status) ? 0xFFE53935 : 0xFF00A86B);
        statusView.setPadding(0, dpToPx(8), 0, 0);
        content.addView(statusView);

        if (!"cancelled".equals(status)) {
            TextView cancelButton = new TextView(requireContext());
            cancelButton.setText("Annuler");
            cancelButton.setGravity(Gravity.CENTER);
            cancelButton.setTextSize(15);
            cancelButton.setTypeface(Typeface.DEFAULT_BOLD);
            cancelButton.setTextColor(0xFFFFFFFF);
            cancelButton.setBackgroundColor(0xFFE53935);
            cancelButton.setPadding(0, dpToPx(10), 0, dpToPx(10));

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.setMargins(0, dpToPx(14), 0, 0);
            cancelButton.setLayoutParams(btnParams);

            cancelButton.setOnClickListener(v ->
                    cancelReservation(reservationId, trajetId, placesReservees)
            );

            content.addView(cancelButton);
        }

        card.addView(content);
        activitiesContainer.addView(card);
    }

    private void cancelReservation(String reservationId, String trajetId, long placesReservees) {
        db.collection("reservations")
                .document(reservationId)
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {
                    if (trajetId != null && !trajetId.isEmpty()) {
                        restoreTrajetAvailability(trajetId, placesReservees);
                    }

                    Toast.makeText(requireContext(), "Trajet annulé", Toast.LENGTH_SHORT).show();
                    loadUserActivities();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Annulation impossible : " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void restoreTrajetAvailability(String trajetId, long placesReservees) {
        db.collection("trajets")
                .document(trajetId)
                .get()
                .addOnSuccessListener(doc -> {
                    Long currentPlacesLong = doc.getLong("places");
                    int currentPlaces = currentPlacesLong == null ? 0 : currentPlacesLong.intValue();

                    int newPlaces = currentPlaces + (int) placesReservees;

                    db.collection("trajets")
                            .document(trajetId)
                            .update(
                                    "places", newPlaces,
                                    "status", "available"
                            );
                });
    }

    private void showEmptyMessage() {
        TextView emptyView = new TextView(requireContext());
        emptyView.setText("Aucune activité pour le moment.");
        emptyView.setTextSize(15);
        emptyView.setTextColor(0xFF777777);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(0, dpToPx(32), 0, 0);

        activitiesContainer.addView(emptyView);
    }

    private String getStringValue(DocumentSnapshot doc, String field, String defaultValue) {
        String value = doc.getString(field);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    private long getLongValue(DocumentSnapshot doc, String field, long defaultValue) {
        Long value = doc.getLong(field);
        return value == null ? defaultValue : value;
    }

    private double getDoubleValue(DocumentSnapshot doc, String field, double defaultValue) {
        Double value = doc.getDouble(field);
        if (value != null) return value;

        Long longValue = doc.getLong(field);
        return longValue == null ? defaultValue : longValue.doubleValue();
    }

    private String getStatusLabel(String status) {
        if ("cancelled".equals(status)) {
            return "Annulé";
        }
        if ("confirmed".equals(status)) {
            return "Confirmé";
        }
        return status;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}