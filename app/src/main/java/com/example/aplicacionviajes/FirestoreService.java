package com.example.aplicacionviajes;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.aplicacionviajes.entity.UserProfile;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class FirestoreService {

    private static final String TAG = "FirestoreService";
    private static final String COLLECTION_USERS = "users";

    public interface ProfileCallback {
        void onSuccess(UserProfile userProfile);
        void onFailure(Exception e);
    }

    private final FirebaseFirestore firestore;
    private ListenerRegistration profileListener;

    public FirestoreService() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    // Obtener perfil una sola vez
    public void getProfile(String userId, ProfileCallback callback) {
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            profile.setUid(documentSnapshot.getId());
                        }
                        Log.d(TAG, "Perfil obtenido correctamente para: " + userId);
                        callback.onSuccess(profile);
                    } else {
                        Log.w(TAG, "No existe perfil para el usuario: " + userId);
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener el perfil: " + e.getMessage());
                    callback.onFailure(e);
                });
    }

    // Crear o actualizar perfil completo
    public void saveProfile(UserProfile profile) {
        DocumentReference docRef = firestore.collection(COLLECTION_USERS).document(profile.getUid());
        docRef.set(profile)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Perfil guardado correctamente para: " + profile.getUid()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al guardar el perfil: " + e.getMessage()));
    }

    // Actualizar campos concretos del perfil
    public void updateProfile(@NonNull String userId, @NonNull String nombre, @NonNull String photoUrl) {
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update("nombre", nombre, "photoUrl", photoUrl)
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Perfil actualizado correctamente para: " + userId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar el perfil: " + e.getMessage()));
    }

    // Eliminar perfil
    public void deleteProfile(String userId) {
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "Perfil eliminado correctamente para: " + userId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al eliminar el perfil: " + e.getMessage()));
    }

    // Escuchar cambios en tiempo real del perfil
    public void listenToProfile(String userId, ProfileCallback callback) {
        if (profileListener != null) return;
        profileListener = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al escuchar cambios del perfil: " + error.getMessage());
                        callback.onFailure(error);
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                        if (profile != null) {
                            profile.setUid(documentSnapshot.getId());
                        }
                        Log.d(TAG, "Cambio detectado en el perfil de: " + userId);
                        callback.onSuccess(profile);
                    } else {
                        Log.w(TAG, "El perfil no existe o fue eliminado: " + userId);
                        callback.onSuccess(null);
                    }
                });
    }

    // Desuscribirse del listener en tiempo real (llamar desde onPause/onStop)
    public void onPause() {
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
            Log.d(TAG, "Listener del perfil desuscrito correctamente");
        }
    }
}
