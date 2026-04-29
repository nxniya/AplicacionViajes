package com.example.aplicacionviajes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.aplicacionviajes.entity.UserProfile;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final String AUTHORITY = "com.example.aplicacionviajes.fileprovider";

    private ImageView ivProfilePhoto;
    private TextView tvEmail;
    private TextInputEditText etNombre;

    private FirebaseUser currentUser;
    private FirestoreService firestoreService;
    private UserProfile currentProfile;
    private Uri cameraImageUri;

    // ─── Activity Result Launchers ─────────────────────────────────────────────

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraImageUri != null) {
                    Glide.with(this).load(cameraImageUri).circleCrop().into(ivProfilePhoto);
                    compressAndSaveToFirestore(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            });

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvEmail = findViewById(R.id.tvEmail);
        etNombre = findViewById(R.id.etNombre);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnChangePhoto = findViewById(R.id.btnChangePhoto);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnLogout = findViewById(R.id.btnLogout);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreService = new FirestoreService();

        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            loadProfile();
        }

        btnBack.setOnClickListener(v -> finish());

        btnChangePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnSave.setOnClickListener(v -> saveProfileName());

        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.label_sign_out)
                        .setMessage(R.string.confirm_sign_out)
                        .setPositiveButton(R.string.label_sign_out, (dialog, which) -> logout())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show());
    }

    // ─── Profile loading ───────────────────────────────────────────────────────

    private void loadProfile() {
        firestoreService.getProfile(currentUser.getUid(), new FirestoreService.ProfileCallback() {
            @Override
            public void onSuccess(UserProfile profile) {
                currentProfile = profile;
                if (profile == null) return;
                if (profile.getNombre() != null) {
                    etNombre.setText(profile.getNombre());
                }
                // Prefer local Base64 photo; fall back to photoUrl (e.g. Google Sign-In avatar)
                if (profile.getPhotoBase64() != null && !profile.getPhotoBase64().isEmpty()) {
                    showBase64Photo(profile.getPhotoBase64());
                } else if (profile.getPhotoUrl() != null && !profile.getPhotoUrl().isEmpty()) {
                    Glide.with(ProfileActivity.this)
                            .load(profile.getPhotoUrl())
                            .circleCrop()
                            .placeholder(R.drawable.ic_person)
                            .into(ivProfilePhoto);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error cargando perfil: " + e.getMessage());
            }
        });
    }

    // ─── Camera ────────────────────────────────────────────────────────────────

    private void launchCamera() {
        try {
            File photoFile = File.createTempFile("profile_", ".jpg", getCacheDir());
            cameraImageUri = FileProvider.getUriForFile(this, AUTHORITY, photoFile);
            takePictureLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Log.e(TAG, "Error creando archivo temporal: " + e.getMessage());
            Toast.makeText(this, "Error al preparar la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Compress → Base64 → Firestore ────────────────────────────────────────

    private void compressAndSaveToFirestore(Uri imageUri) {
        new Thread(() -> {
            String base64 = compressBitmapToBase64(imageUri);
            if (base64 == null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show());
                return;
            }
            if (currentProfile == null) {
                String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
                currentProfile = new UserProfile(currentUser.getUid(), nombre, currentUser.getEmail(), "");
            }
            currentProfile.setPhotoBase64(base64);
            firestoreService.saveProfile(currentProfile);
            runOnUiThread(() -> Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show());
        }).start();
    }

    /** Scales the image down to 200×200 and encodes as Base64 JPEG (≈10–20 KB). */
    private String compressBitmapToBase64(Uri imageUri) {
        try (InputStream in = getContentResolver().openInputStream(imageUri)) {
            Bitmap original = BitmapFactory.decodeStream(in);
            if (original == null) return null;
            Bitmap scaled = Bitmap.createScaledBitmap(original, 200, 200, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error comprimiendo imagen: " + e.getMessage());
            return null;
        }
    }

    private void showBase64Photo(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Glide.with(this).load(bitmap).circleCrop().into(ivProfilePhoto);
    }

    // ─── Save name ─────────────────────────────────────────────────────────────

    private void saveProfileName() {
        if (currentUser == null) return;
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        if (nombre.isEmpty()) {
            etNombre.setError("Introduce un nombre");
            return;
        }
        if (currentProfile == null) {
            currentProfile = new UserProfile(currentUser.getUid(), nombre, currentUser.getEmail(), "");
        } else {
            currentProfile.setNombre(nombre);
        }
        firestoreService.saveProfile(currentProfile);
        Toast.makeText(this, "Nombre guardado", Toast.LENGTH_SHORT).show();
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        getSharedPreferences(AuthActivity.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(AuthActivity.KEY_REMEMBER_ME, false)
                .apply();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
