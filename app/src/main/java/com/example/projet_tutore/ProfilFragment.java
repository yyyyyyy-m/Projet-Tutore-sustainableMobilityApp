package com.example.projet_tutore;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button btnAction;

    // Éléments XML
    private LinearLayout statsLayout, compteLayout, activiteLayout;
    private TextView tvUserEmail;

    public ProfilFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();

        // Récupération des vues
        btnAction = view.findViewById(R.id.logout);
        statsLayout = view.findViewById(R.id.stats_layout);
        compteLayout = view.findViewById(R.id.compte_layout);
        activiteLayout = view.findViewById(R.id.activite_layout);
        tvUserEmail = view.findViewById(R.id.tv_user_email);

        // Mise à jour de l'interface
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        return view;
    }

    // Gère l'affichage selon connexion
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            // Pas connecté
            btnAction.setText("Se connecter");
            statsLayout.setVisibility(View.GONE);
            compteLayout.setVisibility(View.GONE);
            activiteLayout.setVisibility(View.GONE);

            btnAction.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            });

        } else {
            // Connecté
            btnAction.setText("Déconnecter");
            statsLayout.setVisibility(View.VISIBLE);
            compteLayout.setVisibility(View.VISIBLE);
            activiteLayout.setVisibility(View.VISIBLE);

            // Affichage email
            tvUserEmail.setText(currentUser.getEmail());

            // Déconnexion
            btnAction.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                if (getActivity() != null) getActivity().finish();
            });
        }
    }

    // Actualise la page quand on revient
    @Override
    public void onResume() {
        super.onResume();
        updateUI(mAuth.getCurrentUser());
    }
}