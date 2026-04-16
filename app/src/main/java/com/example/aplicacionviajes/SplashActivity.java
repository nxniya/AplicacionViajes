package com.example.aplicacionviajes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aplicacionviajes.entity.Trip;

import java.util.ArrayList;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Generamos los viajes aleatorios
        ArrayList<Trip> listaViajes = Constantes.generateRandomTrips(40);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, SelectorActivity.class);
            intent.putParcelableArrayListExtra("lista_viajes", listaViajes);
            startActivity(intent);
            finish();
        }, 1000);
    }
}
