package com.example.aplicacionviajes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class SelectorActivity extends AppCompatActivity {

    private static final int OPCION_LISTADO = 0;
    private static final int OPCION_SELECCIONADOS = 1;

    private ListView lvMenu;
    private View loadingOverlay;
    private ImageView btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);

        lvMenu = findViewById(R.id.lvMenu);
        loadingOverlay = findViewById(R.id.loadingOverlay);

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
        lvMenu.setAdapter(adapter);

        lvMenu.setOnItemClickListener((parent, view, position, id) -> {
            boolean soloFavoritos = (position == OPCION_SELECCIONADOS);
            Intent intent = new Intent(this, ListadoActivity.class);
            intent.putExtra("solo_favoritos", soloFavoritos);
            startActivity(intent);
        });

        // Seed Firebase and re-enable menu only when seeding is confirmed
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setMenuEnabled(false);
            FirebaseDatabaseService dbService = new FirebaseDatabaseService(user.getUid());
            dbService.initTripsIfEmpty(
                    Constantes.generateRandomTrips(20),
                    () -> FavoritesManager.clearAllFavorites(this), // only when actually seeding
                    () -> runOnUiThread(() -> setMenuEnabled(true))
            );
        }

        btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfilePhoto();
    }

    private void loadProfilePhoto() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        new FirestoreService().getProfile(user.getUid(), new FirestoreService.ProfileCallback() {
            @Override
            public void onSuccess(com.example.aplicacionviajes.entity.UserProfile profile) {
                if (profile == null) return;
                if (profile.getPhotoBase64() != null && !profile.getPhotoBase64().isEmpty()) {
                    byte[] bytes = Base64.decode(profile.getPhotoBase64(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Glide.with(SelectorActivity.this)
                            .load(bitmap)
                            .circleCrop()
                            .into(btnProfile);
                } else if (profile.getPhotoUrl() != null && !profile.getPhotoUrl().isEmpty()) {
                    Glide.with(SelectorActivity.this)
                            .load(profile.getPhotoUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(btnProfile);
                }
            }

            @Override
            public void onFailure(Exception e) { /* keep default icon */ }
        });
    }

    private void setMenuEnabled(boolean enabled) {
        lvMenu.setEnabled(enabled);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(enabled ? View.GONE : View.VISIBLE);
        }
    }
}

