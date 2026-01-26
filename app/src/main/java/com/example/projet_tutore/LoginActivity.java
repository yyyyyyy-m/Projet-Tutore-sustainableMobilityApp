package com.example.projet_tutore;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private Button btnConnexion;
    private Button btnSansConnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Initialisation des composants
        btnConnexion = findViewById(R.id.btnContinue);
        btnSansConnexion = findViewById(R.id.btnsansConnexion);

        // 2. Vérification automatique de la session au démarrage
        checkExistingSession();

        // 3. Action du bouton Connexion
        btnConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ici, on simule une connexion réussie
                String fakeToken = "abc123secure_token";

                // On enregistre le token de manière sécurisée
                SessionManager.getInstance(LoginActivity.this).saveAuthToken(fakeToken);

                Toast.makeText(LoginActivity.this, "Connecté avec succès !", Toast.LENGTH_SHORT).show();
                goToHome();
            }
        });

        // 4. Action du bouton Continuer sans connexion
        btnSansConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome();
            }
        });
    }
//kdzsaLKkbx
    private void checkExistingSession() {
        String token = SessionManager.getInstance(this).getAuthToken();
        if (token != null) {
            // L'utilisateur est déjà venu, on l'envoie direct à l'accueil
            goToHome();// test
        }
    }//mxslk

    private void goToHome() {
        // Remplace HomeActivity par le nom de ton activité principale
        // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        // startActivity(intent);
        // finish(); // Ferme MainActivity pour ne pas revenir en arrière

        Toast.makeText(this, "Redirection vers l'accueil...", Toast.LENGTH_SHORT).show();
    }
}