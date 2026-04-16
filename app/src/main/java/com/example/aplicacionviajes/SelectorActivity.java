package com.example.aplicacionviajes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aplicacionviajes.entity.Trip;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class SelectorActivity extends AppCompatActivity {

    private ArrayList<Trip> listaViajes;
    private ActivityResultLauncher<Intent> listadoLauncher;

    private static final int OPCION_LISTADO = 0;
    private static final int OPCION_SELECCIONADOS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        // Recuperar la lista inicial (desde SplashActivity)
        if (getIntent().hasExtra("lista_viajes")) {
            listaViajes = getIntent().getParcelableArrayListExtra("lista_viajes");
        }

        if (listaViajes == null) {
            listaViajes = Constantes.generateRandomTrips(20);
        }

        FavoritesManager.applyFavorites(this, listaViajes);

        listadoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Actualizar la lista con los cambios (favoritos) que vuelvan de ListadoActivity
                        listaViajes = result.getData().getParcelableArrayListExtra("lista_viajes");
                    }
                }
        );

        List<SelectorAdapter.Item> opciones = Arrays.asList(
                new SelectorAdapter.Item(
                        "Ver viajes disponibles",
                        "Explora todos los destinos",
                        R.drawable.ic_explore,
                        R.drawable.bg_icon_teal
                ),
                new SelectorAdapter.Item(
                        "Ver viajes seleccionados",
                        "Tus viajes favoritos",
                        R.drawable.ic_favorites,
                        R.drawable.bg_icon_purple
                )
        );

        SelectorAdapter adapter = new SelectorAdapter(this, opciones);
        ListView lvMenu = findViewById(R.id.lvMenu);
        lvMenu.setAdapter(adapter);

        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            boolean soloFavoritos = (position == OPCION_SELECCIONADOS);
            Intent intent = new Intent(this, ListadoActivity.class);
            intent.putParcelableArrayListExtra("lista_viajes", listaViajes);
            intent.putExtra("solo_favoritos", soloFavoritos);
            listadoLauncher.launch(intent);
        });
    }
}
