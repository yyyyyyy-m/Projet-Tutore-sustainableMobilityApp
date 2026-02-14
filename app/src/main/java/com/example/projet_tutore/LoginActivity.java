package com.example.projet_tutore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Assurez-vous que ce layout existe

        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnContinue);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Ici, vous pouvez ajouter votre logique d'authentification
                if (validerConnexion(email, password)) {
                    // Connexion réussie -> aller vers MainActivity
                    Intent intent = new Intent(LoginActivity.this,UtilisateurActivity.class);
                    startActivity(intent);
                    finish(); // Ferme LoginActivity
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Email ou mot de passe incorrect",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validerConnexion(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }
}