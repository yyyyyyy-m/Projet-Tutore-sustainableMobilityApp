package com.example.projet_tutore;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    /*
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Assurez-vous que ce layout existe

        // Handler pour démarrer LoginActivity après 2 secondes (ligne 19)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Vérifier si il est connecté
            boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

            // connecté: à MainActivity, non connecté: à LoginActivity
            Intent intent = new Intent(
                    SplashActivity.this,
                    isLoggedIn ? MainActivity.class : LoginActivity.class
            );

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();// Ferme SplashActivity pour éviter de revenir en arrière
        }, 2000); // Délai de 2 secondes
    }
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {

                user.reload().addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Intent intent = new Intent(
                                SplashActivity.this,
                                MainActivity.class
                        );

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    } else {

                        startActivity(new Intent(
                                SplashActivity.this,
                                LoginActivity.class
                        ));
                        finish();
                    }
                });

            } else {

                Intent intent = new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                );

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

        }, 2000);
    }
}