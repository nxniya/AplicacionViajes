package com.example.aplicacionviajes.restTypes;

import com.google.gson.annotations.SerializedName;

/** Represents the {@code main} block in the OpenWeatherMap response (temperature, humidity, etc.). */
public class WeatherMain {

    @SerializedName("temp")
    private double temp;

    @SerializedName("feels_like")
    private double feelsLike;

    @SerializedName("temp_min")
    private double tempMin;

    @SerializedName("temp_max")
    private double tempMax;

    @SerializedName("pressure")
    private int pressure;

    @SerializedName("humidity")
    private int humidity;

    public WeatherMain() {}

    public WeatherMain(double temp, double feelsLike, double tempMin, double tempMax,
                       int pressure, int humidity) {
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.pressure = pressure;
        this.humidity = humidity;
    }

    public double getTemp() { return temp; }
    public void setTemp(double temp) { this.temp = temp; }

    public double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(double feelsLike) { this.feelsLike = feelsLike; }

    public double getTempMin() { return tempMin; }
    public void setTempMin(double tempMin) { this.tempMin = tempMin; }

    public double getTempMax() { return tempMax; }
    public void setTempMax(double tempMax) { this.tempMax = tempMax; }

    public int getPressure() { return pressure; }
    public void setPressure(int pressure) { this.pressure = pressure; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
}
