package com.example.aplicacionviajes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacionviajes.entity.Trip;

import java.util.ArrayList;

public class ListadoActivity extends AppCompatActivity {

    private ArrayList<Trip> listaOriginal;
    private ArrayList<Trip> listaFiltrada;
    private ViajesAdapter customAdapter;
    private RecyclerView recyclerView;
    private boolean isGridView = false;

    // Estados de los filtros
    private String filterCity = "";
    private float minPrice = 0;
    private float maxPrice = 2000;
    private long minTimestamp = 0;
    private long maxTimestamp = Long.MAX_VALUE;
    private boolean soloFavoritos = false;

    private ActivityResultLauncher<Intent> filterLauncher;
    private ActivityResultLauncher<Intent> detailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_listado);

        if (getIntent().hasExtra("lista_viajes")) {
            listaOriginal = getIntent().getParcelableArrayListExtra("lista_viajes");
        }
        soloFavoritos = getIntent().getBooleanExtra("solo_favoritos", false);

        if (listaOriginal == null) listaOriginal = new ArrayList<>();

        // Launcher para Filtros
        filterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        filterCity = data.getStringExtra("city");
                        minPrice = data.getFloatExtra("min_price", 0);
                        maxPrice = data.getFloatExtra("max_price", 2000);
                        minTimestamp = data.getLongExtra("min_date", 0);
                        maxTimestamp = data.getLongExtra("max_date", Long.MAX_VALUE);
                        soloFavoritos = data.getBooleanExtra("solo_favoritos", false);
                        applyFilters();
                    }
                }
        );

        // Launcher para Detalle (para capturar cambios en favoritos)
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Trip updatedTrip = result.getData().getParcelableExtra("trip");
                        if (updatedTrip != null) {
                            updateTripInList(updatedTrip);
                        }
                    }
                }
        );

        setupUI();
        applyFilters();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                returnResult();
            }
        });
    }

    private void updateTripInList(Trip updatedTrip) {
        for (int i = 0; i < listaOriginal.size(); i++) {
            if (listaOriginal.get(i).getCodigo().equals(updatedTrip.getCodigo())) {
                listaOriginal.set(i, updatedTrip);
                break;
            }
        }
        applyFilters();
    }

    private void returnResult() {
        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra("lista_viajes", listaOriginal);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void setupUI() {
        Button btnOpenFilter = findViewById(R.id.btnOpenFilter);
        Button btnCambiarVista = findViewById(R.id.buttonCambiarVista);
        recyclerView = findViewById(R.id.recyclerViewViajes);

        btnOpenFilter.setOnClickListener(v -> {
            Intent intent = new Intent(this, FilterActivity.class);
            intent.putExtra("city", filterCity);
            intent.putExtra("min_price", minPrice);
            intent.putExtra("max_price", maxPrice);
            intent.putExtra("min_date", minTimestamp);
            intent.putExtra("max_date", maxTimestamp);
            intent.putExtra("solo_favoritos", soloFavoritos);
            filterLauncher.launch(intent);
        });

        btnCambiarVista.setOnClickListener(v -> {
            isGridView = !isGridView;
            updateLayoutManager();
        });

        listaFiltrada = new ArrayList<>();
        customAdapter = new ViajesAdapter(listaFiltrada, trip -> {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("trip", trip);
            detailLauncher.launch(intent);
        });

        recyclerView.setAdapter(customAdapter);
        updateLayoutManager();
    }

    private void updateLayoutManager() {
        if (isGridView) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        customAdapter.setGridMode(isGridView);
        customAdapter.notifyDataSetChanged();
    }

    private void applyFilters() {
        listaFiltrada.clear();
        for (Trip t : listaOriginal) {
            boolean matchFav = !soloFavoritos || t.isFavorite();
            boolean matchPrice = t.getPrecio() >= minPrice && t.getPrecio() <= maxPrice;
            boolean matchDate = t.getFecha() >= minTimestamp && t.getFecha() <= maxTimestamp;
            boolean matchCity = (filterCity == null || filterCity.isEmpty()) || 
                                t.getCiudad().toLowerCase().contains(filterCity.toLowerCase());

            if (matchFav && matchPrice && matchDate && matchCity) {
                listaFiltrada.add(t);
            }
        }
        customAdapter.notifyDataSetChanged();
    }
}
