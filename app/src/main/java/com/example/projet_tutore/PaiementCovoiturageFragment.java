package com.example.projet_tutore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

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

public class PaiementCovoiturageFragment extends Fragment {

    public PaiementCovoiturageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paiement_covoiturage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvBack = view.findViewById(R.id.tvBack);
        TextView tvInitials = view.findViewById(R.id.tvInitials);
        TextView tvDriverName = view.findViewById(R.id.tvDriverName);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvTripInfo = view.findViewById(R.id.tvTripInfo);
        TextView tvPlaces = view.findViewById(R.id.tvPlaces);
        TextView tvPoints = view.findViewById(R.id.tvPoints);

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnPay = view.findViewById(R.id.btnPay);

        Bundle args = getArguments();

        if (args != null) {
            String initials = args.getString("initials", "SM");
            String driverName = args.getString("driverName", "Sophie Martin");
            String departure = args.getString("departure", "Châtelet Les Halles");
            String arrival = args.getString("arrival", "Gare du Nord");
            String time = args.getString("time", "14:30");
            String places = args.getString("places", "2");
            String price = args.getString("price", "5");
            String points = args.getString("points", "+25");

            tvInitials.setText(initials);
            tvDriverName.setText(driverName);
            tvPrice.setText(price + "€\npar place");
            tvTripInfo.setText("●  " + departure + "\n│   Aujourd’hui • " + time + "\n●  " + arrival);
            tvPlaces.setText(places + "\nplaces");
            tvPoints.setText(points + "\npoints");
        }

        tvBack.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnCancel.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnPay.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Paiement effectué avec succès", Toast.LENGTH_SHORT).show()
        );
    }
}