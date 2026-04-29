package com.example.aplicacionviajes.restTypes;

import com.google.gson.annotations.SerializedName;

public class WeatherCoord {

    @SerializedName("lon")
    private double lon;

    @SerializedName("lat")
    private double lat;

    public WeatherCoord() {}

    public WeatherCoord(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
}
