package com.example.projet_tutore;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class ProfilFragment extends Fragment {

    private FirebaseAuth mAuth;
    private Button logout;

    public ProfilFragment() {
        // constructeur vide obligatoire
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        // Initialiser Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Récupérer le bouton
        logout = view.findViewById(R.id.logout);

        // Action quand on clique sur logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Déconnexion
                mAuth.signOut();

                // Aller vers l'activité de login
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }
}