package com.example.aplicacionviajes.restTypes;

import com.google.gson.annotations.SerializedName;

/** Represents the {@code wind} block in the OpenWeatherMap response. */
public class WeatherWind {

    @SerializedName("speed")
    private double speed;

    @SerializedName("deg")
    private int deg;

    public WeatherWind() {}

    public WeatherWind(double speed, int deg) {
        this.speed = speed;
        this.deg = deg;
    }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public int getDeg() { return deg; }
    public void setDeg(int deg) { this.deg = deg; }
}
