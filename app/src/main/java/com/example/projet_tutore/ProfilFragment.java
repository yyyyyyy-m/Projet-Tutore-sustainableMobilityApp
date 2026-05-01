package com.example.projet_tutore;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button btnAction;

    public ProfilFragment() {
        // constructeur vide obligatoire
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Récupérer l'utilisateur connecté

        btnAction = view.findViewById(R.id.logout);

        if (currentUser == null) {
            // UTILISATEUR NON CONNECTÉ
            btnAction.setText("S'inscrire");
            btnAction.setOnClickListener(v -> {
                // Redirection vers Inscription
                Intent intent = new Intent(getActivity(), InscriptionActivity.class);
                startActivity(intent);
            });
        } else {
            // UTILISATEUR CONNECTÉ : Déconnexion
            btnAction.setText("Déconnecter");
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