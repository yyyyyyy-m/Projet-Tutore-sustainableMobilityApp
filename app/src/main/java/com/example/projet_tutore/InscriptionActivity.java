package com.example.projet_tutore;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
public class InscriptionActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmMdpET;
    private Button signupButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialisation Firebase
        auth = FirebaseAuth.getInstance();

        // Liaison avec le XML
        emailEditText = findViewById(R.id.signupEmail);
        passwordEditText = findViewById(R.id.mdp);
        confirmMdpET = findViewById(R.id.confirmMdp);
        signupButton = findViewById(R.id.signup);

        signupButton.setOnClickListener(v -> inscription());
    }

    private void inscription() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmMdpET.getText().toString().trim();

        // Vérifications
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mot de passe minimum 6 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création utilisateur Firebase
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Inscription réussie 🎉", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Erreur : " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}