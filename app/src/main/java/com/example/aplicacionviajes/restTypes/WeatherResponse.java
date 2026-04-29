package com.example.aplicacionviajes.restTypes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResponse {

    @SerializedName("coord")
    private WeatherCoord coord;

    @SerializedName("weather")
    private List<WeatherCondition> weather;

    @SerializedName("base")
    private String base;

    @SerializedName("main")
    private WeatherMain main;

    @SerializedName("wind")
    private WeatherWind wind;

    @SerializedName("name")
    private String name;

    @SerializedName("cod")
    private int cod;

    public WeatherResponse() {}

    public WeatherResponse(WeatherCoord coord, List<WeatherCondition> weather, String base,
                           WeatherMain main, WeatherWind wind, String name, int cod) {
        this.coord = coord;
        this.weather = weather;
        this.base = base;
        this.main = main;
        this.wind = wind;
        this.name = name;
        this.cod = cod;
    }

    public WeatherCoord getCoord() { return coord; }
    public void setCoord(WeatherCoord coord) { this.coord = coord; }

    public List<WeatherCondition> getWeather() { return weather; }
    public void setWeather(List<WeatherCondition> weather) { this.weather = weather; }

    public String getBase() { return base; }
    public void setBase(String base) { this.base = base; }

    public WeatherMain getMain() { return main; }
    public void setMain(WeatherMain main) { this.main = main; }

    public WeatherWind getWind() { return wind; }
    public void setWind(WeatherWind wind) { this.wind = wind; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCod() { return cod; }
    public void setCod(int cod) { this.cod = cod; }
}
