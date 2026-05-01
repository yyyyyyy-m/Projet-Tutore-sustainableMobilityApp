package com.example.projet_tutore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ResultatCovoiturageFragment extends Fragment {

    private FirebaseFirestore db;

    private String depart;
    private String destination;
    private String date;
    private String time;
    private String passengers;

    private int passengersCount = 1;

    private TextView tvFrom;
    private TextView tvTo;
    private TextView tvDateTime;
    private TextView tvPassengers;
    private TextView tvResultCount;
    private TextView tvSectionToday;
    private LinearLayout llResultsContainer;

    public ResultatCovoiturageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resultat_covoiturage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        TextView tvClose = view.findViewById(R.id.tvClose);
        TextView tvEditSearch = view.findViewById(R.id.tvEditSearch);

        tvFrom = view.findViewById(R.id.tvFrom);
        tvTo = view.findViewById(R.id.tvTo);
        tvDateTime = view.findViewById(R.id.tvDateTime);
        tvPassengers = view.findViewById(R.id.tvPassengers);
        tvResultCount = view.findViewById(R.id.tvResultCount);
        tvSectionToday = view.findViewById(R.id.tvSectionToday);
        llResultsContainer = view.findViewById(R.id.llResultsContainer);

        readArguments();
        showSearchSummary();
        loadResultsFromFirebase();

        tvClose.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        tvEditSearch.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );
    }

    private void readArguments() {
        Bundle args = getArguments();

        if (args != null) {
            depart = args.getString("depart", "");
            destination = args.getString("destination", "");
            date = args.getString("date", "");
            time = args.getString("time", "");
            passengers = args.getString("passengers", "1");
        } else {
            depart = "";
            destination = "";
            date = "";
            time = "";
            passengers = "1";
        }

        try {
            passengersCount = Integer.parseInt(passengers);
        } catch (NumberFormatException e) {
            passengersCount = 1;
        }
    }

    private void showSearchSummary() {
        tvFrom.setText("De  " + depart);
        tvTo.setText("À  " + destination);
        tvDateTime.setText("Départ : " + date + ", " + time);
        tvPassengers.setText("Nombre de passagers : " + passengers);
    }

    private void loadResultsFromFirebase() {
        llResultsContainer.removeAllViews();
        tvResultCount.setText("Recherche en cours...");
        tvSectionToday.setVisibility(View.GONE);

        db.collection("trajets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Trajet trajet = documentToTrajet(document);

                        if (matchesSearch(trajet)) {
                            count++;
                            addTrajetCard(trajet);
                        }
                    }

                    if (count == 0) {
                        tvResultCount.setText("Aucun conducteur trouvé pour cet itinéraire.");
                        tvSectionToday.setVisibility(View.GONE);
                    } else {
                        tvResultCount.setText(count + " conducteur(s) avec correspondance d’itinéraire !");
                        tvSectionToday.setText(formatDateLabel(date));
                        tvSectionToday.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    tvResultCount.setText("Erreur lors du chargement des résultats.");
                    Toast.makeText(requireContext(), "Erreur Firebase : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private Trajet documentToTrajet(DocumentSnapshot document) {
        Trajet trajet = new Trajet();

        trajet.id = document.getId();

        trajet.driverName = getStringValue(document, "driverName", "Conducteur");
        trajet.initials = getStringValue(document, "initials", "CD");

        trajet.depart = getStringValue(document, "depart", "");
        trajet.destination = getStringValue(document, "destination", "");
        trajet.departLower = getStringValue(document, "departLower", trajet.depart.toLowerCase(Locale.ROOT));
        trajet.destinationLower = getStringValue(document, "destinationLower", trajet.destination.toLowerCase(Locale.ROOT));

        trajet.date = getStringValue(document, "date", "");
        trajet.time = getStringValue(document, "time", "");

        Long placesLong = document.getLong("places");
        trajet.places = placesLong == null ? 0 : placesLong.intValue();

        Double priceDouble = document.getDouble("price");
        if (priceDouble == null) {
            Long priceLong = document.getLong("price");
            trajet.price = priceLong == null ? 0 : priceLong.doubleValue();
        } else {
            trajet.price = priceDouble;
        }

        trajet.status = getStringValue(document, "status", "available");

        return trajet;
    }

    private String getStringValue(DocumentSnapshot document, String key, String defaultValue) {
        String value = document.getString(key);
        return value == null ? defaultValue : value;
    }

    private boolean matchesSearch(Trajet trajet) {
        boolean sameDepart = trajet.departLower.equals(depart.toLowerCase(Locale.ROOT));
        boolean sameDestination = trajet.destinationLower.equals(destination.toLowerCase(Locale.ROOT));
        boolean sameDate = trajet.date.equals(date);
        boolean enoughPlaces = trajet.places >= passengersCount;
        boolean available = !"completed".equals(trajet.status);

        return sameDepart && sameDestination && sameDate && enoughPlaces && available;
    }

    private void addTrajetCard(Trajet trajet) {
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_covoiturage_trajet, llResultsContainer, false);

        TextView tvInitials = card.findViewById(R.id.tvInitials);
        TextView tvDriverName = card.findViewById(R.id.tvDriverName);
        TextView tvPrice = card.findViewById(R.id.tvPrice);
        TextView tvDeparture = card.findViewById(R.id.tvDeparture);
        TextView tvTime = card.findViewById(R.id.tvTime);
        TextView tvDestination = card.findViewById(R.id.tvDestination);
        TextView tvPlaces = card.findViewById(R.id.tvPlaces);
        TextView btnChoose = card.findViewById(R.id.btnChoose);

        tvInitials.setText(trajet.initials);
        tvDriverName.setText(trajet.driverName);
        tvPrice.setText(formatPrice(trajet.price) + "€\npar place");

        tvDeparture.setText(trajet.depart);
        tvTime.setText(formatDateLabel(trajet.date) + " • " + trajet.time);
        tvDestination.setText(trajet.destination);

        tvPlaces.setText(trajet.places + "\nplaces");

        btnChoose.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("documentId", trajet.id);

            bundle.putString("driverName", trajet.driverName);
            bundle.putString("initials", trajet.initials);
            bundle.putString("departure", trajet.depart);
            bundle.putString("arrival", trajet.destination);
            bundle.putString("date", trajet.date);
            bundle.putString("time", trajet.time);
            bundle.putString("places", String.valueOf(trajet.places));
            bundle.putString("price", formatPrice(trajet.price));
            bundle.putString("passengers", String.valueOf(passengersCount));

            Navigation.findNavController(v)
                    .navigate(R.id.paiementCovoiturageFragment, bundle);
        });

        llResultsContainer.addView(card);
    }

    private String formatPrice(double price) {
        if (price == (int) price) {
            return String.valueOf((int) price);
        }
        return String.valueOf(price);
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
            } catch (ParseException ignored) {
            }
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