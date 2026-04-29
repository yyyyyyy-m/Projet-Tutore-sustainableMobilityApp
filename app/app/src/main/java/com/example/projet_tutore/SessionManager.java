package com.example.projet_tutore;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {
    private static SessionManager instance;
    private SharedPreferences sharedPreferences;

    private SessionManager(Context context) {
        try {
            // 1. Création de la Master Key
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // AES256_GCM Algo de chiffrement
                    .build();

            // 2. Initialisation des SharedPreferences chiffrées
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,//SharedPreferences
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Nous chiffrons les SharedPreferences afin de protéger le caractère secret des mots de passe
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // Nous utilisons la méthode synchronized afin de bloquer la création de 2 fils d'execution en meme temps
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }
    // Méthode de gestion CRUD Lecture , Ecriture ,supression des mots de passe
    public void saveAuthToken(String token) {
        sharedPreferences.edit().putString("auth_token", token).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString("auth_token", null);
    }

    public void logout() {
        sharedPreferences.edit().clear().apply(); // Ecriture asynchrone pour éviter les lags
    }
}