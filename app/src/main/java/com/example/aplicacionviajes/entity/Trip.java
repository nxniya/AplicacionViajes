package com.example.aplicacionviajes.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.Objects;

public class Trip implements Parcelable {

    private String titulo, codigo, ciudad, imageUrl;
    private double precio;
    private long fecha; // Timestamp
    private boolean isFavorite;
    private double latitude;
    private double longitude;

    @Exclude
    private String firebaseKey;

    // No-arg constructor required by Firebase Realtime Database
    public Trip() {}

    public Trip(String titulo, String codigo, String ciudad, double precio, long fecha, String imageUrl, double latitude, double longitude) {
        this.titulo = titulo;
        this.codigo = codigo;
        this.ciudad = ciudad;
        this.precio = precio;
        this.fecha = fecha;
        this.imageUrl = imageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFavorite = false;
    }

    // Backward-compatible constructor (no coordinates)
    public Trip(String titulo, String codigo, String ciudad, double precio, long fecha, String imageUrl) {
        this(titulo, codigo, ciudad, precio, fecha, imageUrl, 0.0, 0.0);
    }

    protected Trip(Parcel in) {
        titulo = in.readString();
        codigo = in.readString();
        ciudad = in.readString();
        imageUrl = in.readString();
        precio = in.readDouble();
        fecha = in.readLong();
        isFavorite = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        firebaseKey = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    // Getters
    public String getTitulo() { return titulo; }
    public String getCodigo() { return codigo; }
    public String getCiudad() { return ciudad; }
    public String getImageUrl() { return imageUrl; }
    public double getPrecio() { return precio; }
    public long getFecha() { return fecha; }
    @Exclude public boolean isFavorite() { return isFavorite; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    @Exclude public String getFirebaseKey() { return firebaseKey; }

    // Setters (required by Firebase Realtime Database deserialization)
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setFecha(long fecha) { this.fecha = fecha; }
    @Exclude public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    @Exclude public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }

    @NonNull
    @Override
    public String toString() {
        return titulo + " (" + ciudad + ") - " + precio + "€";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(codigo, trip.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(codigo);
        dest.writeString(ciudad);
        dest.writeString(imageUrl);
        dest.writeDouble(precio);
        dest.writeLong(fecha);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(firebaseKey);
    }
}

