package com.example.projet_tutore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Assurez-vous que ce layout existe

        // Handler pour démarrer LoginActivity après 2 secondes (ligne 19)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Ferme SplashActivity pour éviter de revenir en arrière
            }
        }, 2000); // Délai de 2 secondes
    }
}