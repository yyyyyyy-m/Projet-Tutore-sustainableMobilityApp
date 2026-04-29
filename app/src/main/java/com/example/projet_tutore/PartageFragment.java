package com.example.projet_tutore;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PartageFragment extends Fragment {

    private TextView btnSearchMode, btnPublishMode;
    private TextView btnSearchTrip, btnPublishTrip;
    private ImageView btnHistory;

    private LinearLayout layoutSearch, layoutPublish;

    private EditText etDepartSearch, etDestinationSearch, etDateSearch, etTimeSearch, etPassengersSearch;
    private EditText etDepartPublish, etDestinationPublish, etDatePublish, etTimePublish, etPlacesPublish, etPricePublish;

    private FirebaseFirestore db;

    public PartageFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_partage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupActions();

        Bundle args = getArguments();
        if (args != null && "publish".equals(args.getString("mode"))) {
            showPublishMode();
        } else {
            showSearchMode();
        }
    }

    private void initViews(View view) {
        btnSearchMode = view.findViewById(R.id.btnSearchMode);
        btnPublishMode = view.findViewById(R.id.btnPublishMode);
        btnSearchTrip = view.findViewById(R.id.btnSearchTrip);
        btnPublishTrip = view.findViewById(R.id.btnPublishTrip);
        btnHistory = view.findViewById(R.id.btnHistory);

        layoutSearch = view.findViewById(R.id.layoutSearch);
        layoutPublish = view.findViewById(R.id.layoutPublish);

        etDepartSearch = view.findViewById(R.id.etDepartSearch);
        etDestinationSearch = view.findViewById(R.id.etDestinationSearch);
        etDateSearch = view.findViewById(R.id.etDateSearch);
        etTimeSearch = view.findViewById(R.id.etTimeSearch);
        etPassengersSearch = view.findViewById(R.id.etPassengersSearch);

        etDepartPublish = view.findViewById(R.id.etDepartPublish);
        etDestinationPublish = view.findViewById(R.id.etDestinationPublish);
        etDatePublish = view.findViewById(R.id.etDatePublish);
        etTimePublish = view.findViewById(R.id.etTimePublish);
        etPlacesPublish = view.findViewById(R.id.etPlacesPublish);
        etPricePublish = view.findViewById(R.id.etPricePublish);
    }

    private void setupActions() {
        btnSearchMode.setOnClickListener(v -> showSearchMode());
        btnPublishMode.setOnClickListener(v -> showPublishMode());

        btnHistory.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.activitesCovoiturageFragment);
        });

        btnSearchTrip.setOnClickListener(v -> searchTrip(v));

        btnPublishTrip.setOnClickListener(v -> publishTrip());
    }

    private void searchTrip(View v) {
        String depart = etDepartSearch.getText().toString().trim();
        String destination = etDestinationSearch.getText().toString().trim();
        String date = etDateSearch.getText().toString().trim();
        String time = etTimeSearch.getText().toString().trim();
        String passengers = etPassengersSearch.getText().toString().trim();

        if (TextUtils.isEmpty(depart) || TextUtils.isEmpty(destination)
                || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)
                || TextUtils.isEmpty(passengers)) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int passengersInt = Integer.parseInt(passengers);
            if (passengersInt <= 0) {
                Toast.makeText(requireContext(), "Nombre de passagers invalide", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Nombre de passagers invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("depart", depart);
        bundle.putString("destination", destination);
        bundle.putString("date", date);
        bundle.putString("time", time);
        bundle.putString("passengers", passengers);

        Navigation.findNavController(v)
                .navigate(R.id.resultatCovoiturageFragment, bundle);
    }

    private void publishTrip() {
        String depart = etDepartPublish.getText().toString().trim();
        String destination = etDestinationPublish.getText().toString().trim();
        String date = etDatePublish.getText().toString().trim();
        String time = etTimePublish.getText().toString().trim();
        String places = etPlacesPublish.getText().toString().trim();
        String price = etPricePublish.getText().toString().trim();

        if (TextUtils.isEmpty(depart) || TextUtils.isEmpty(destination)
                || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)
                || TextUtils.isEmpty(places) || TextUtils.isEmpty(price)) {
            Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        int placesInt;
        double priceDouble;

        try {
            placesInt = Integer.parseInt(places);
            priceDouble = Double.parseDouble(price.replace(",", "."));

            if (placesInt <= 0 || priceDouble < 0) {
                Toast.makeText(requireContext(), "Places ou prix invalide", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Places ou prix invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> trajet = new HashMap<>();
        trajet.put("driverName", "Conducteur");
        trajet.put("initials", "CD");

        trajet.put("depart", depart);
        trajet.put("destination", destination);
        trajet.put("departLower", depart.toLowerCase(Locale.ROOT));
        trajet.put("destinationLower", destination.toLowerCase(Locale.ROOT));

        trajet.put("date", date);
        trajet.put("time", time);
        trajet.put("places", placesInt);
        trajet.put("price", priceDouble);
        trajet.put("status", "available");
        trajet.put("createdAt", FieldValue.serverTimestamp());

        db.collection("trajets")
                .add(trajet)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Trajet publié avec succès", Toast.LENGTH_SHORT).show();

                    etDepartPublish.setText("");
                    etDestinationPublish.setText("");
                    etDatePublish.setText("");
                    etTimePublish.setText("");
                    etPlacesPublish.setText("");
                    etPricePublish.setText("");

                    showSearchMode();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Erreur Firebase : " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showSearchMode() {
        layoutSearch.setVisibility(View.VISIBLE);
        layoutPublish.setVisibility(View.GONE);

        btnSearchMode.setBackgroundResource(R.drawable.bg_tab_selected);
        btnSearchMode.setTextColor(0xFF111827);

        btnPublishMode.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnPublishMode.setTextColor(0xFF6B7280);
    }

    private void showPublishMode() {
        layoutSearch.setVisibility(View.GONE);
        layoutPublish.setVisibility(View.VISIBLE);

        btnPublishMode.setBackgroundResource(R.drawable.bg_tab_selected);
        btnPublishMode.setTextColor(0xFF111827);

        btnSearchMode.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        btnSearchMode.setTextColor(0xFF6B7280);
    }
}