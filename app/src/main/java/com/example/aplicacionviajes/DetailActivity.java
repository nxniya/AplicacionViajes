package com.example.aplicacionviajes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.aplicacionviajes.entity.Trip;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        trip = getIntent().getParcelableExtra("trip");

        View root = findViewById(R.id.detailRoot);
        ImageView detailImage = findViewById(R.id.detailImage);
        TextView title = findViewById(R.id.detailTitle);
        TextView ciudad = findViewById(R.id.detailCiudad);
        TextView codigo = findViewById(R.id.detailCodigo);
        TextView precio = findViewById(R.id.detailPrecio);
        TextView fecha = findViewById(R.id.detailFecha);
        CheckBox checkFavorite = findViewById(R.id.checkFavorite);
        Button btnBuy = findViewById(R.id.btnBuy);

        if (trip != null) {
            title.setText(trip.getTitulo());
            ciudad.setText("Ciudad: " + trip.getCiudad());
            codigo.setText("Código: " + trip.getCodigo());
            precio.setText(String.format(Locale.getDefault(), "Precio: %.2f€", trip.getPrecio()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            fecha.setText("Fecha: " + sdf.format(new Date(trip.getFecha())));

            Glide.with(this)
                    .load(trip.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .centerCrop()
                    .into(detailImage);

            checkFavorite.setChecked(trip.isFavorite());
            checkFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
                trip.setFavorite(isChecked);
                FavoritesManager.saveFavoriteCode(DetailActivity.this, trip.getCodigo(), isChecked);
            });

            btnBuy.setOnClickListener(v -> {
                String message = "¡Que tengas un buen viaje en " + trip.getCiudad() + "!";
                Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("trip", trip);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
