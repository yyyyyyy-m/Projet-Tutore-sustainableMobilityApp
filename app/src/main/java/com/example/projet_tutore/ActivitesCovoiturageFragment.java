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

public class ActivitesCovoiturageFragment extends Fragment {

    public ActivitesCovoiturageFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activites_covoiturage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView btnClose = view.findViewById(R.id.btnClose);
        TextView btnCancelSophie = view.findViewById(R.id.btnCancelSophie);

        btnClose.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        btnCancelSophie.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Trajet annulé", Toast.LENGTH_SHORT).show()
        );
    }
}