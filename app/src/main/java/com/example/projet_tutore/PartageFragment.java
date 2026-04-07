package com.example.projet_tutore;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class PartageFragment extends Fragment {

    private Button btnSearchMode, btnPublishMode;
    private Button btnSearchTrip, btnPublishTrip;
    private LinearLayout layoutSearch, layoutPublish;

    private EditText etDepartSearch, etDestinationSearch, etDateSearch, etTimeSearch, etPassengersSearch;
    private EditText etDepartPublish, etDestinationPublish, etDatePublish, etTimePublish, etPlacesPublish, etPricePublish;

    public PartageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_partage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToggleButtons();
        setupActions();
    }

    private void initViews(View view) {
        btnSearchMode = view.findViewById(R.id.btnSearchMode);
        btnPublishMode = view.findViewById(R.id.btnPublishMode);

        btnSearchTrip = view.findViewById(R.id.btnSearchTrip);
        btnPublishTrip = view.findViewById(R.id.btnPublishTrip);

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

    private void setupToggleButtons() {
        btnSearchMode.setOnClickListener(v -> showSearchMode());
        btnPublishMode.setOnClickListener(v -> showPublishMode());
    }

    private void showSearchMode() {
        layoutSearch.setVisibility(View.VISIBLE);
        layoutPublish.setVisibility(View.GONE);

        btnSearchMode.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnSearchMode.setTextColor(getResources().getColor(android.R.color.black));

        btnPublishMode.setBackgroundColor(0xFFF3F4F6);
        btnPublishMode.setTextColor(0xFF6B7280);
    }

    private void showPublishMode() {
        layoutSearch.setVisibility(View.GONE);
        layoutPublish.setVisibility(View.VISIBLE);

        btnPublishMode.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnPublishMode.setTextColor(getResources().getColor(android.R.color.black));

        btnSearchMode.setBackgroundColor(0xFFF3F4F6);
        btnSearchMode.setTextColor(0xFF6B7280);
    }

    private void setupActions() {
        btnSearchTrip.setOnClickListener(v -> {
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

            Toast.makeText(requireContext(), "Recherche lancée", Toast.LENGTH_SHORT).show();

            // sauter vers ResultatActivity
            // Intent intent = new Intent(requireContext(), ResultatCovoiturageActivity.class);
            // startActivity(intent);
        });

        btnPublishTrip.setOnClickListener(v -> {
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

            Toast.makeText(requireContext(), "Trajet publié avec succès", Toast.LENGTH_SHORT).show();
        });
    }
}