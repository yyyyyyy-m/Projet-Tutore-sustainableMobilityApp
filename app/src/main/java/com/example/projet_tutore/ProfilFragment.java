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

    // Éléments à cacher/afficher + remplir le nom
    private LinearLayout statsLayout, compteLayout, activiteLayout;
    private TextView tvUsername, tvUserEmail;

    public ProfilFragment() {
        // constructeur vide obligatoire
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Récupérer le bouton
        btnAction = view.findViewById(R.id.logout);

        // Récupérer les layouts à CACHER si non connecté
        statsLayout = view.findViewById(R.id.stats_layout);
        compteLayout = view.findViewById(R.id.compte_layout);
        activiteLayout = view.findViewById(R.id.activite_layout);


        tvUserEmail = view.findViewById(R.id.tv_user_email);

        // Vérification connexion
        if (currentUser == null) {
            // UTILISATEUR NON CONNECTÉ : CACHER TOUT SAUF LE BOUTON
            btnAction.setText("S'inscrire");
            statsLayout.setVisibility(View.GONE);
            compteLayout.setVisibility(View.GONE);
            activiteLayout.setVisibility(View.GONE);

            btnAction.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), InscriptionActivity.class);
                startActivity(intent);
            });

        } else {
            // UTILISATEUR CONNECTÉ : AFFICHER TOUT + REMPLIR NOM/EMAIL FIREBASE
            btnAction.setText("Déconnecter");
            statsLayout.setVisibility(View.VISIBLE);
            compteLayout.setVisibility(View.VISIBLE);
            activiteLayout.setVisibility(View.VISIBLE);

            // Remplir automatiquement le profil depuis Firebase
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();

            // Si le nom existe dans Firebase, on l'affiche, sinon l'email
            if (displayName != null && !displayName.isEmpty()) {
                tvUsername.setText(displayName);
            } else {
                tvUsername.setText("Utilisateur");
            }

            tvUserEmail.setText(email);

            // Action déconnexion
            btnAction.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            });
        }

        return view;
    }
}