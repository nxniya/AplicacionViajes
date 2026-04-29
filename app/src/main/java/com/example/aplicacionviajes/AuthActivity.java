package com.example.aplicacionviajes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.example.aplicacionviajes.entity.Trip;
import com.example.aplicacionviajes.entity.UserProfile;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    static final String PREFS_NAME = "auth_prefs";
    static final String KEY_REMEMBER_ME = "remember_me";

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilConfirmPassword;
    private MaterialButton btnAuth, btnToggleMode, btnGoogleSignIn;
    private CircularProgressIndicator progressBar;
    private TextView tvAuthTitle;
    private CheckBox cbRememberMe;

    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);

        tvAuthTitle = findViewById(R.id.tvAuthTitle);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnAuth = findViewById(R.id.btnAuth);
        btnToggleMode = findViewById(R.id.btnToggleMode);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        progressBar = findViewById(R.id.progressBar);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        // Restore last saved preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cbRememberMe.setChecked(prefs.getBoolean(KEY_REMEMBER_ME, true));

        btnAuth.setOnClickListener(v -> {
            if (isRegisterMode) {
                registerWithEmail();
            } else {
                loginWithEmail();
            }
        });

        btnToggleMode.setOnClickListener(v -> toggleMode());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
    }

    // ─── Toggle login / register mode ─────────────────────────────────────────

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            tvAuthTitle.setText(R.string.label_create_account);
            btnAuth.setText(R.string.label_create_account);
            btnToggleMode.setText(R.string.label_sign_in);
            tilConfirmPassword.setVisibility(View.VISIBLE);
            cbRememberMe.setVisibility(View.GONE);
        } else {
            tvAuthTitle.setText(R.string.label_sign_in);
            btnAuth.setText(R.string.label_sign_in);
            btnToggleMode.setText(R.string.label_create_account);
            tilConfirmPassword.setVisibility(View.GONE);
            cbRememberMe.setVisibility(View.VISIBLE);
        }
    }

    // ─── Email authentication ──────────────────────────────────────────────────

    private void loginWithEmail() {
        String email = getValidatedEmail();
        String password = getValidatedPassword();
        if (email == null || password == null) return;

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && !user.isEmailVerified()) {
                            mAuth.signOut();
                            showSnackbar(getString(R.string.error_email_not_verified));
                        } else {
                            saveRememberMe(cbRememberMe.isChecked());
                            navigateToMain();
                        }
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "";
                        showSnackbar(getString(R.string.error_auth_failed, msg));
                    }
                });
    }

    private void registerWithEmail() {
        String email = getValidatedEmail();
        String password = getValidatedPassword();
        if (email == null || password == null) return;

        String confirm = etConfirmPassword.getText() != null
                ? etConfirmPassword.getText().toString().trim() : "";
        if (!confirm.equals(password)) {
            Toast.makeText(this, R.string.error_passwords_no_match, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verifyTask -> {
                                    setLoading(false);
                                    mAuth.signOut();
                                    // Switch to log in mode and show confirmation
                                    if (isRegisterMode) toggleMode();
                                    showSnackbar(getString(R.string.info_verification_sent));
                                });
                    } else {
                        setLoading(false);
                        String msg = task.getException() != null ? task.getException().getMessage() : "";
                        showSnackbar(getString(R.string.error_auth_failed, msg));
                    }
                });
    }

    // ─── Google Sign-In (Credential Manager) ──────────────────────────────────

    private void signInWithGoogle() {
        // default_web_client_id is auto-generated from google-services.json by the google-services plugin
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(false) // auto-select silently fails if no pre-authorized account
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        setLoading(true);
        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        runOnUiThread(() -> handleGoogleCredential(result));
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showSnackbar(getString(R.string.error_google_sign_in, e.getMessage()));
                        });
                    }
                });
    }

    private void handleGoogleCredential(GetCredentialResponse result) {
        if (result.getCredential() instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleCredential = (GoogleIdTokenCredential) result.getCredential();
            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.getIdToken(), null);
            mAuth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this, task -> {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            // Google sign-in always remembers (OAuth manages its own token)
                            saveRememberMe(true);
                            navigateToMain();
                        } else {
                            String msg = task.getException() != null ? task.getException().getMessage() : "";
                            showSnackbar(getString(R.string.error_auth_failed, msg));
                        }
                    });
        } else {
            setLoading(false);
            showSnackbar(getString(R.string.error_unexpected_credential));
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String getValidatedEmail() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (TextUtils.isEmpty(email)) {
            showSnackbar(getString(R.string.error_email_required));
            return null;
        }
        return email;
    }

    private String getValidatedPassword() {
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        if (TextUtils.isEmpty(password)) {
            showSnackbar(getString(R.string.error_password_required));
            return null;
        }
        if (password.length() < 6) {
            showSnackbar(getString(R.string.error_password_too_short));
            return null;
        }
        return password;
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void saveRememberMe(boolean remember) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_REMEMBER_ME, remember)
                .apply();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAuth.setEnabled(!loading);
        btnToggleMode.setEnabled(!loading);
        btnGoogleSignIn.setEnabled(!loading);
    }

    private void navigateToMain() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirestoreService firestoreService = new FirestoreService();
            UserProfile profile = new UserProfile(
                    user.getUid(),
                    user.getDisplayName() != null ? user.getDisplayName() : user.getEmail(),
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : ""
            );
            firestoreService.saveProfile(profile);
        }
        Intent intent = new Intent(this, SelectorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
