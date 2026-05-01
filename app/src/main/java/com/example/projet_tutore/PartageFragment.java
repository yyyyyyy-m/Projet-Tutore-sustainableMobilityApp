package com.example.projet_tutore;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
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
import java.util.Map;

public class PartageFragment extends Fragment {

    private TextView btnSearchMode, btnPublishMode;
    private TextView btnSearchTrip, btnPublishTrip;
    private ImageView btnHistory;

    private LinearLayout layoutSearch, layoutPublish;

    private EditText etDepartSearch, etDestinationSearch, etDateSearch, etTimeSearch, etPassengersSearch;
    private EditText etDepartPublish, etDestinationPublish, etDatePublish, etTimePublish, etPlacesPublish, etPricePublish;

    private FirebaseFirestore db;

    private double departSearchLat = Double.NaN;
    private double departSearchLng = Double.NaN;
    private double destinationSearchLat = Double.NaN;
    private double destinationSearchLng = Double.NaN;

    private double departPublishLat = Double.NaN;
    private double departPublishLng = Double.NaN;
    private double destinationPublishLat = Double.NaN;
    private double destinationPublishLng = Double.NaN;

    private String routePolyline = "";
    private String routeDistance = "";
    private String routeDuration = "";

    private double routeOriginLat = Double.NaN;
    private double routeOriginLng = Double.NaN;
    private double routeDestinationLat = Double.NaN;
    private double routeDestinationLng = Double.NaN;

    public PartageFragment() {}

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
        setupInputFormats();
        setupActions();
        setupMapPickerResultListener();
        setupAddressPickers();
        setupDrivingRouteResultListener();

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

        btnHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.activitesCovoiturageFragment)
        );

        btnSearchTrip.setOnClickListener(v -> searchTrip(v));
        btnPublishTrip.setOnClickListener(v -> publishTrip());
    }

    private void setupInputFormats() {
        setupLocationInput(etDepartSearch);
        setupLocationInput(etDestinationSearch);
        setupLocationInput(etDepartPublish);
        setupLocationInput(etDestinationPublish);

        setupDateInput(etDateSearch);
        setupDateInput(etDatePublish);

        setupTimeInput(etTimeSearch);
        setupTimeInput(etTimePublish);

        setupSmallIntegerInput(etPassengersSearch);
        setupSmallIntegerInput(etPlacesPublish);

        setupPriceInput(etPricePublish);
    }

    private void setupLocationInput(EditText editText) {
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        editText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(80),
                (source, start, end, dest, dstart, dend) -> {
                    String input = source.subSequence(start, end).toString();
                    if (input.matches("[\\p{L}\\p{M}0-9 .'’\\-,]*")) return null;
                    return "";
                }
        });
    }

    private void setupDateInput(EditText editText) {
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        editText.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;

                String digits = s.toString().replaceAll("\\D", "");
                if (digits.length() > 8) digits = digits.substring(0, 8);

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 2 || i == 4) formatted.append("/");
                    formatted.append(digits.charAt(i));
                }

                editText.setText(formatted.toString());
                editText.setSelection(editText.getText().length());

                editing = false;
            }
        });
    }

    private void setupTimeInput(EditText editText) {
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        editText.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;

                String digits = s.toString().replaceAll("\\D", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 2) formatted.append(":");
                    formatted.append(digits.charAt(i));
                }

                editText.setText(formatted.toString());
                editText.setSelection(editText.getText().length());

                editing = false;
            }
        });
    }

    private void setupSmallIntegerInput(EditText editText) {
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        editText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(1),
                (source, start, end, dest, dstart, dend) -> {
                    String newValue = dest.toString().substring(0, dstart)
                            + source.subSequence(start, end)
                            + dest.toString().substring(dend);

                    if (newValue.isEmpty()) return null;
                    if (newValue.matches("[1-8]")) return null;

                    return "";
                }
        });
    }

    private void setupPriceInput(EditText editText) {
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        editText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(6),
                (source, start, end, dest, dstart, dend) -> {
                    String newValue = dest.toString().substring(0, dstart)
                            + source.subSequence(start, end)
                            + dest.toString().substring(dend);

                    if (newValue.isEmpty()) return null;
                    if (newValue.matches("\\d{0,2}([,.]\\d{0,2})?")) return null;

                    return "";
                }
        });
    }

    private void searchTrip(View v) {
        if (!validateSearchInputs()) {
            Toast.makeText(requireContext(), "Veuillez corriger les champs invalides", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasValidCoordinates(departSearchLat, departSearchLng)
                || !hasValidCoordinates(destinationSearchLat, destinationSearchLng)) {
            Toast.makeText(requireContext(),
                    "Veuillez choisir le départ et la destination sur la carte",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String depart = etDepartSearch.getText().toString().trim();
        String destination = etDestinationSearch.getText().toString().trim();
        String date = etDateSearch.getText().toString().trim();
        String time = etTimeSearch.getText().toString().trim();
        String passengers = etPassengersSearch.getText().toString().trim();

        Bundle bundle = new Bundle();
        bundle.putString("depart", depart);
        bundle.putString("destination", destination);
        bundle.putString("date", date);
        bundle.putString("time", time);
        bundle.putString("passengers", passengers);
        bundle.putDouble("departLat", departSearchLat);
        bundle.putDouble("departLng", departSearchLng);
        bundle.putDouble("destinationLat", destinationSearchLat);
        bundle.putDouble("destinationLng", destinationSearchLng);

        Navigation.findNavController(v)
                .navigate(R.id.resultatCovoiturageFragment, bundle);
    }

    private void publishTrip() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(),
                    "Veuillez vous connecter pour proposer un trajet",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!validatePublishInputs()) {
            Toast.makeText(requireContext(), "Veuillez corriger les champs invalides", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasValidCoordinates(departPublishLat, departPublishLng)
                || !hasValidCoordinates(destinationPublishLat, destinationPublishLng)) {
            Toast.makeText(requireContext(),
                    "Veuillez choisir le départ et la destination sur la carte",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (routePolyline == null || routePolyline.isEmpty()) {
            openDrivingRouteForPublish();
            return;
        }

        String depart = etDepartPublish.getText().toString().trim();
        String destination = etDestinationPublish.getText().toString().trim();
        String date = etDatePublish.getText().toString().trim();
        String time = etTimePublish.getText().toString().trim();
        String places = etPlacesPublish.getText().toString().trim();
        String price = etPricePublish.getText().toString().trim();

        int placesInt = Integer.parseInt(places);
        double priceDouble = Double.parseDouble(price.replace(",", "."));

        Map<String, Object> trajet = new HashMap<>();
        trajet.put("driverId", user.getUid());
        trajet.put("driverName", user.getEmail() != null ? user.getEmail() : "Conducteur");
        trajet.put("initials", getInitialsFromUser(user));

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

        trajet.put("departLat", departPublishLat);
        trajet.put("departLng", departPublishLng);
        trajet.put("destinationLat", destinationPublishLat);
        trajet.put("destinationLng", destinationPublishLng);

        trajet.put("routePolyline", routePolyline);
        trajet.put("routeDistance", routeDistance);
        trajet.put("routeDuration", routeDuration);
        trajet.put("routeOriginLat", routeOriginLat);
        trajet.put("routeOriginLng", routeOriginLng);
        trajet.put("routeDestinationLat", routeDestinationLat);
        trajet.put("routeDestinationLng", routeDestinationLng);
        trajet.put("travelMode", "driving");

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

                    departPublishLat = Double.NaN;
                    departPublishLng = Double.NaN;
                    destinationPublishLat = Double.NaN;
                    destinationPublishLng = Double.NaN;
                    resetConfirmedRoute();

                    showSearchMode();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Erreur Firebase : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void openDrivingRouteForPublish() {
        Bundle bundle = new Bundle();

        bundle.putString("origin", etDepartPublish.getText().toString().trim());
        bundle.putString("destination", etDestinationPublish.getText().toString().trim());

        bundle.putDouble("originLat", departPublishLat);
        bundle.putDouble("originLng", departPublishLng);
        bundle.putDouble("destinationLat", destinationPublishLat);
        bundle.putDouble("destinationLng", destinationPublishLng);

        Navigation.findNavController(requireView())
                .navigate(R.id.drivingRouteFragment, bundle);
    }

    private void setupDrivingRouteResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                "driving_route_result",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    routePolyline = result.getString("routePolyline", "");
                    routeDistance = result.getString("routeDistance", "");
                    routeDuration = result.getString("routeDuration", "");

                    routeOriginLat = result.getDouble("routeOriginLat");
                    routeOriginLng = result.getDouble("routeOriginLng");
                    routeDestinationLat = result.getDouble("routeDestinationLat");
                    routeDestinationLng = result.getDouble("routeDestinationLng");

                    Toast.makeText(requireContext(),
                            "Itinéraire confirmé, publication du trajet...",
                            Toast.LENGTH_SHORT).show();

                    showPublishMode();
                    publishTrip();
                }
        );
    }

    private void resetConfirmedRoute() {
        routePolyline = "";
        routeDistance = "";
        routeDuration = "";

        routeOriginLat = Double.NaN;
        routeOriginLng = Double.NaN;
        routeDestinationLat = Double.NaN;
        routeDestinationLng = Double.NaN;
    }

    private String getInitialsFromUser(FirebaseUser user) {
        String email = user.getEmail();

        if (email == null || email.trim().isEmpty()) return "CD";

        String namePart = email.split("@")[0].trim();

        if (namePart.length() >= 2) return namePart.substring(0, 2).toUpperCase(Locale.ROOT);
        if (namePart.length() == 1) return namePart.toUpperCase(Locale.ROOT);

        return "CD";
    }

    private boolean validateSearchInputs() {
        boolean ok = true;

        if (!validateLocation(etDepartSearch, "Départ")) ok = false;
        if (!validateLocation(etDestinationSearch, "Destination")) ok = false;
        if (!validateDate(etDateSearch, "Date")) ok = false;
        if (!validateTime(etTimeSearch, "Heure")) ok = false;
        if (!validateSmallInteger(etPassengersSearch, "Nombre de passagers")) ok = false;

        return ok;
    }

    private boolean validatePublishInputs() {
        boolean ok = true;

        if (!validateLocation(etDepartPublish, "Départ")) ok = false;
        if (!validateLocation(etDestinationPublish, "Destination")) ok = false;
        if (!validateDate(etDatePublish, "Date")) ok = false;
        if (!validateTime(etTimePublish, "Heure")) ok = false;
        if (!validateSmallInteger(etPlacesPublish, "Places disponibles")) ok = false;
        if (!validatePrice(etPricePublish, "Prix")) ok = false;

        return ok;
    }

    private boolean validateLocation(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            editText.setError(fieldName + " obligatoire");
            return false;
        }

        if (!value.matches(".*[\\p{L}].*")) {
            editText.setError(fieldName + " doit contenir au moins une lettre");
            return false;
        }

        if (value.length() < 2) {
            editText.setError(fieldName + " trop court");
            return false;
        }

        editText.setError(null);
        return true;
    }

    private boolean validateDate(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            editText.setError(fieldName + " obligatoire");
            return false;
        }

        if (!value.matches("\\d{2}/\\d{2}/\\d{4}")) {
            editText.setError("Format attendu : jj/mm/aaaa");
            return false;
        }

        Date parsedDate = parseDateStrict(value);

        if (parsedDate == null) {
            editText.setError("Date invalide");
            return false;
        }

        Calendar selected = Calendar.getInstance();
        selected.setTime(parsedDate);
        clearTime(selected);

        Calendar today = Calendar.getInstance();
        clearTime(today);

        if (selected.before(today)) {
            editText.setError("La date ne peut pas être passée");
            return false;
        }

        editText.setError(null);
        return true;
    }

    private boolean validateTime(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            editText.setError(fieldName + " obligatoire");
            return false;
        }

        if (!value.matches("\\d{2}:\\d{2}")) {
            editText.setError("Format attendu : HH:mm");
            return false;
        }

        String[] parts = value.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            editText.setError("Heure invalide");
            return false;
        }

        editText.setError(null);
        return true;
    }

    private boolean validateSmallInteger(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim();

        if (value.isEmpty()) {
            editText.setError(fieldName + " obligatoire");
            return false;
        }

        try {
            int number = Integer.parseInt(value);

            if (number < 1 || number > 8) {
                editText.setError(fieldName + " doit être entre 1 et 8");
                return false;
            }

        } catch (NumberFormatException e) {
            editText.setError(fieldName + " invalide");
            return false;
        }

        editText.setError(null);
        return true;
    }

    private boolean validatePrice(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim().replace(",", ".");

        if (value.isEmpty()) {
            editText.setError(fieldName + " obligatoire");
            return false;
        }

        if (!value.matches("\\d{1,2}(\\.\\d{1,2})?")) {
            editText.setError("Prix invalide. Exemple : 10 ou 10.50");
            return false;
        }

        try {
            double price = Double.parseDouble(value);

            if (price < 0 || price > 99.99) {
                editText.setError("Prix entre 0 et 99.99");
                return false;
            }

        } catch (NumberFormatException e) {
            editText.setError("Prix invalide");
            return false;
        }

        editText.setError(null);
        return true;
    }

    private Date parseDateStrict(String value) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
            sdf.setLenient(false);
            return sdf.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupAddressPickers() {
        etDepartSearch.setFocusable(false);
        etDestinationSearch.setFocusable(false);
        etDepartPublish.setFocusable(false);
        etDestinationPublish.setFocusable(false);

        etDepartSearch.setOnClickListener(v ->
                openMapPicker("departSearch", etDepartSearch.getText().toString().trim()));

        etDestinationSearch.setOnClickListener(v ->
                openMapPicker("destinationSearch", etDestinationSearch.getText().toString().trim()));

        etDepartPublish.setOnClickListener(v ->
                openMapPicker("departPublish", etDepartPublish.getText().toString().trim()));

        etDestinationPublish.setOnClickListener(v ->
                openMapPicker("destinationPublish", etDestinationPublish.getText().toString().trim()));
    }

    private void openMapPicker(String target, String initialAddress) {
        Bundle bundle = new Bundle();
        bundle.putString("target", target);
        bundle.putString("initialAddress", initialAddress);

        Navigation.findNavController(requireView())
                .navigate(R.id.mapPickerFragment, bundle);
    }

    private void setupMapPickerResultListener() {
        getParentFragmentManager().setFragmentResultListener(
                "map_picker_result",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String target = result.getString("target", "");
                    String address = result.getString("address", "");
                    double lat = result.getDouble("lat");
                    double lng = result.getDouble("lng");

                    switch (target) {
                        case "departSearch":
                            showSearchMode();
                            etDepartSearch.setText(address);
                            departSearchLat = lat;
                            departSearchLng = lng;
                            break;

                        case "destinationSearch":
                            showSearchMode();
                            etDestinationSearch.setText(address);
                            destinationSearchLat = lat;
                            destinationSearchLng = lng;
                            break;

                        case "departPublish":
                            showPublishMode();
                            etDepartPublish.setText(address);
                            departPublishLat = lat;
                            departPublishLng = lng;
                            resetConfirmedRoute();
                            break;

                        case "destinationPublish":
                            showPublishMode();
                            etDestinationPublish.setText(address);
                            destinationPublishLat = lat;
                            destinationPublishLng = lng;
                            resetConfirmedRoute();
                            break;
                    }
                }
        );
    }

    private boolean hasValidCoordinates(double lat, double lng) {
        return !Double.isNaN(lat) && !Double.isNaN(lng);
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