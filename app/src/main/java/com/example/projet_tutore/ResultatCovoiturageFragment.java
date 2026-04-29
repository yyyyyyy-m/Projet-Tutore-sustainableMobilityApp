package com.example.projet_tutore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class ResultatCovoiturageFragment extends Fragment {

    public ResultatCovoiturageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resultat_covoiturage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvFrom = view.findViewById(R.id.tvFrom);
        TextView tvTo = view.findViewById(R.id.tvTo);
        TextView tvDateTime = view.findViewById(R.id.tvDateTime);
        TextView tvPassengers = view.findViewById(R.id.tvPassengers);
        TextView tvEditSearch = view.findViewById(R.id.tvEditSearch);

        TextView tabProposerTrajet = view.findViewById(R.id.tabProposerTrajet);

        TextView btnChooseSophie = view.findViewById(R.id.btnChooseSophie);
        TextView btnChooseJean = view.findViewById(R.id.btnChooseJean);

        Bundle args = getArguments();
        if (args != null) {
            tvFrom.setText("De   " + args.getString("depart", ""));
            tvTo.setText("À    " + args.getString("destination", ""));
            tvDateTime.setText("Départ : " + args.getString("date", "") + ", " + args.getString("time", ""));
            tvPassengers.setText("Nombre de passagers : " + args.getString("passengers", ""));
        }

        tvEditSearch.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        tabProposerTrajet.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("mode", "publish");

            Navigation.findNavController(v)
                    .navigate(R.id.partageFragment, bundle);
        });

        btnChooseSophie.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("driverName", "Sophie Martin");
            bundle.putString("initials", "SM");
            bundle.putString("departure", "Châtelet Les Halles");
            bundle.putString("arrival", "Gare du Nord");
            bundle.putString("time", "14:30");
            bundle.putString("places", "2");
            bundle.putString("price", "5");
            bundle.putString("points", "+25");

            Navigation.findNavController(v)
                    .navigate(R.id.paiementCovoiturageFragment, bundle);
        });

        btnChooseJean.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("driverName", "Jean Dupont");
            bundle.putString("initials", "JD");
            bundle.putString("departure", "Louvre");
            bundle.putString("arrival", "Gare du Nord");
            bundle.putString("time", "16:00");
            bundle.putString("places", "3");
            bundle.putString("price", "4");
            bundle.putString("points", "+20");

            Navigation.findNavController(v)
                    .navigate(R.id.paiementCovoiturageFragment, bundle);
        });
    }
}