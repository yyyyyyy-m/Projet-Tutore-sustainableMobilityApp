package com.example.projet_tutore;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, sansConnexion;
    private FirebaseAuth auth;

    private TextView sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Assurez-vous que ce layout existe
        auth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.etEmail);
        passwordEditText = findViewById(R.id.etPassword);
        loginButton = findViewById(R.id.btnContinue);
        sign = findViewById(R.id.creer);
        sansConnexion = findViewById(R.id.btnsansConnexion);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sign.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, InscriptionActivity.class);
            startActivity(intent);
        });

        sansConnexion.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        });


        sign.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, InscriptionActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Ici, vous pouvez ajouter votre logique d'authentification
                if(email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if(task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    /**
    private boolean validerConnexion(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

     **/
}