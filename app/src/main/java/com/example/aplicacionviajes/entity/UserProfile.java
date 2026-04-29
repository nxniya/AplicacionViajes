package com.example.aplicacionviajes.entity;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {

    private String uid;
    private String nombre;
    private String email;
    private String photoUrl;
    private String photoBase64; // compressed 200x200 JPEG stored in Firestore

    // Constructor vacío requerido por Firestore
    public UserProfile() {}

    public UserProfile(String uid, String nombre, String email, String photoUrl) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    @Exclude
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
}
