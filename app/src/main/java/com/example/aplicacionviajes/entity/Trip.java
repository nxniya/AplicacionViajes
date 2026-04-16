package com.example.aplicacionviajes.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Trip implements Parcelable {

    private String titulo, codigo, ciudad, imageUrl;
    private double precio;
    private long fecha; // Timestamp
    private boolean isFavorite;

    public Trip(String titulo, String codigo, String ciudad, double precio, long fecha, String imageUrl) {
        this.titulo = titulo;
        this.codigo = codigo;
        this.ciudad = ciudad;
        this.precio = precio;
        this.fecha = fecha;
        this.imageUrl = imageUrl;
        this.isFavorite = false;
    }

    protected Trip(Parcel in) {
        titulo = in.readString();
        codigo = in.readString();
        ciudad = in.readString();
        imageUrl = in.readString();
        precio = in.readDouble();
        fecha = in.readLong();
        isFavorite = in.readByte() != 0;
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

    public String getTitulo() { return titulo; }
    public String getCodigo() { return codigo; }
    public String getCiudad() { return ciudad; }
    public String getImageUrl() { return imageUrl; }
    public double getPrecio() { return precio; }
    public long getFecha() { return fecha; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

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
    }
}
