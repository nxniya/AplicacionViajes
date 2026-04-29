package com.example.aplicacionviajes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aplicacionviajes.entity.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class ListadoActivity extends AppCompatActivity {

    private ArrayList<Trip> listaFiltrada;
    private ViajesAdapter customAdapter;
    private RecyclerView recyclerView;
    private View progressOverlay;
    private boolean isGridView = false;

    private FirebaseDatabaseService dbService;

    // Filter state
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

        soloFavoritos = getIntent().getBooleanExtra("solo_favoritos", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dbService = new FirebaseDatabaseService(user.getUid());
        }

        filterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        filterCity = data.getStringExtra("city") != null ? data.getStringExtra("city") : "";
                        minPrice = data.getFloatExtra("min_price", 0);
                        maxPrice = data.getFloatExtra("max_price", 2000);
                        minTimestamp = data.getLongExtra("min_date", 0);
                        maxTimestamp = data.getLongExtra("max_date", Long.MAX_VALUE);
                        soloFavoritos = data.getBooleanExtra("solo_favoritos", false);
                        loadTripsFromDatabase();
                    }
                }
        );

        // Detail launcher: favorites are persisted in SharedPreferences, no result needed
        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { /* no-op */ }
        );

        setupUI();
        loadTripsFromDatabase();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When showing only favorites, refresh after returning from DetailActivity
        // so any favorite toggle is immediately reflected
        if (soloFavoritos) {
            loadTripsFromDatabase();
        }
    }

    // ─── Firebase loading ───────────────────────────────────────────────────────

    private void loadTripsFromDatabase() {
        if (dbService == null) return;
        setLoading(true);

        FirebaseDatabaseService.TripsCallback callback = new FirebaseDatabaseService.TripsCallback() {
            @Override
            public void onSuccess(ArrayList<Trip> trips) {
                FavoritesManager.applyFavorites(ListadoActivity.this, trips);
                applyLocalFilters(trips);
                setLoading(false);
            }
            @Override
            public void onFailure(DatabaseError error) {
                setLoading(false);
                Toast.makeText(ListadoActivity.this, "Error al cargar viajes", Toast.LENGTH_SHORT).show();
            }
        };

        // Primary filter delegated to Firebase; secondary filters applied client-side
        if (filterCity != null && !filterCity.isEmpty()) {
            dbService.getTripsByCity(filterCity, callback);
        } else if (minTimestamp > 0 || maxTimestamp < Long.MAX_VALUE) {
            dbService.getTripsByDateRange(minTimestamp, maxTimestamp, callback);
        } else if (minPrice > 0 || maxPrice < 2000) {
            dbService.getTripsByPriceRange(minPrice, maxPrice, callback);
        } else {
            dbService.getAllTrips(callback);
        }
    }

    private void applyLocalFilters(ArrayList<Trip> trips) {
        listaFiltrada.clear();
        for (Trip t : trips) {
            boolean matchFav   = !soloFavoritos || t.isFavorite();
            boolean matchPrice = t.getPrecio() >= minPrice && t.getPrecio() <= maxPrice;
            boolean matchDate  = t.getFecha() >= minTimestamp && t.getFecha() <= maxTimestamp;
            boolean matchCity  = (filterCity == null || filterCity.isEmpty()) ||
                                  t.getCiudad().toLowerCase().contains(filterCity.toLowerCase());
            if (matchFav && matchPrice && matchDate && matchCity) {
                listaFiltrada.add(t);
            }
        }
        customAdapter.notifyDataSetChanged();
    }

    // ─── UI setup ───────────────────────────────────────────────────────────────

    private void setupUI() {
        Button btnOpenFilter = findViewById(R.id.btnOpenFilter);
        Button btnCambiarVista = findViewById(R.id.buttonCambiarVista);
        recyclerView = findViewById(R.id.recyclerViewViajes);
        progressOverlay = findViewById(R.id.progressOverlay);

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

    private void setLoading(boolean loading) {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }
}

