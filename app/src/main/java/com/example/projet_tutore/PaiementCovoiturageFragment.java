package com.example.projet_tutore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PaiementCovoiturageFragment extends Fragment {

    private FirebaseFirestore db;

    private String documentId = "";
    private int passengersCount = 1;

    private String departure = "";
    private String arrival = "";
    private String date = "";
    private String time = "";
    private String price = "";

    public PaiementCovoiturageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paiement_covoiturage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        TextView tvBack = view.findViewById(R.id.tvBack);
        TextView tvInitials = view.findViewById(R.id.tvInitials);
        TextView tvDriverName = view.findViewById(R.id.tvDriverName);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvTripInfo = view.findViewById(R.id.tvTripInfo);
        TextView tvPlaces = view.findViewById(R.id.tvPlaces);

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnPay = view.findViewById(R.id.btnPay);

        Bundle args = getArguments();

        if (args != null) {
            documentId = args.getString("documentId", "");

            String initials = args.getString("initials", "CD");
            String driverName = args.getString("driverName", "Conducteur");
            departure = args.getString("departure", "");
            arrival = args.getString("arrival", "");
            date = args.getString("date", "");
            time = args.getString("time", "");
            String places = args.getString("places", "1");
            price = args.getString("price", "0");
            String passengers = args.getString("passengers", "1");

            try {
                passengersCount = Integer.parseInt(passengers);
            } catch (NumberFormatException e) {
                passengersCount = 1;
            }

            tvInitials.setText(initials);
            tvDriverName.setText(driverName);
            tvPrice.setText(price + "€\npar place");
            tvTripInfo.setText("●  " + departure
                    + "\n│   " + formatDateLabel(date) + " • " + time
                    + "\n●  " + arrival);
            tvPlaces.setText(places + "\nplaces");
        }

        tvBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnPay.setOnClickListener(v -> confirmReservation(v));
    }

    private void confirmReservation(View v) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(),
                    "Veuillez vous connecter pour réserver",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(requireContext(), "Erreur : trajet introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("trajets")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Long currentPlacesLong = documentSnapshot.getLong("places");

                    if (currentPlacesLong == null) {
                        Toast.makeText(requireContext(), "Erreur : places indisponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int currentPlaces = currentPlacesLong.intValue();

                    if (currentPlaces < passengersCount) {
                        Toast.makeText(requireContext(), "Pas assez de places disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newPlaces = currentPlaces - passengersCount;

                    db.collection("trajets")
                            .document(documentId)
                            .update(
                                    "places", newPlaces,
                                    "status", newPlaces == 0 ? "completed" : "available"
                            )
                            .addOnSuccessListener(unused -> {
                                saveReservation(user);

                                Toast.makeText(requireContext(),
                                        "Réservation confirmée",
                                        Toast.LENGTH_SHORT).show();

                                Navigation.findNavController(v)
                                        .popBackStack(R.id.partageFragment, false);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Erreur réservation : " + e.getMessage(),
                                            Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Erreur Firebase : " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private void saveReservation(FirebaseUser user) {
        double parsedPrice = 0.0;

        try {
            parsedPrice = Double.parseDouble(price);
        } catch (Exception ignored) {}

        HashMap<String, Object> reservation = new HashMap<>();
        reservation.put("userId", user.getUid());
        reservation.put("trajetId", documentId);
        reservation.put("depart", departure);
        reservation.put("arrivee", arrival);
        reservation.put("date", date);
        reservation.put("heure", time);
        reservation.put("prix", parsedPrice);
        reservation.put("placesReservees", passengersCount);
        reservation.put("status", "confirmed");
        reservation.put("createdAt", FieldValue.serverTimestamp());

        db.collection("reservations").add(reservation);
    }

    private String formatDateLabel(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "";
        }

        Date tripDate = parseDate(rawDate.trim());

        if (tripDate == null) {
            return rawDate;
        }

        Calendar trip = Calendar.getInstance();
        trip.setTime(tripDate);
        clearTime(trip);

        Calendar today = Calendar.getInstance();
        clearTime(today);

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (isSameDay(trip, today)) {
            return "Aujourd’hui";
        }

        if (isSameDay(trip, tomorrow)) {
            return "Demain";
        }

        if (isSameDay(trip, yesterday)) {
            return "Hier";
        }

        return new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(tripDate);
    }

    private Date parseDate(String rawDate) {
        String[] formats = {
                "dd/MM/yyyy",
                "d/M/yyyy",
                "yyyy-MM-dd"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.FRANCE);
                sdf.setLenient(false);
                return sdf.parse(rawDate);
            } catch (ParseException ignored) {}
        }

        return null;
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}