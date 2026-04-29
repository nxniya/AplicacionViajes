package com.example.aplicacionviajes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aplicacionviajes.entity.Trip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            boolean rememberMe = getSharedPreferences(AuthActivity.PREFS_NAME, MODE_PRIVATE)
                    .getBoolean(AuthActivity.KEY_REMEMBER_ME, true);

            boolean loggedIn = auth.getCurrentUser() != null
                    && (auth.getCurrentUser().isEmailVerified()
                        || auth.getCurrentUser().getProviderData().stream()
                               .anyMatch(p -> !"password".equals(p.getProviderId())));

            if (!loggedIn || !rememberMe) {
                // Not authenticated, unverified, or chose not to be remembered
                auth.signOut();
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            } else {
                // Fully authenticated and remembered — generate trips and go to main screen
                ArrayList<Trip> listaViajes = Constantes.generateRandomTrips(40);
                Intent intent = new Intent(SplashActivity.this, SelectorActivity.class);
                intent.putParcelableArrayListExtra("lista_viajes", listaViajes);
                startActivity(intent);
            }
            finish();
        }, 1000);
    }
}
