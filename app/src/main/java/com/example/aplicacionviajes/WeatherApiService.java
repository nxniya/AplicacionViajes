package com.example.aplicacionviajes;

import com.example.aplicacionviajes.restTypes.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    /**
     * GET https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={key}&units={units}
     *
     * @param lat     Latitude of the location.
     * @param lon     Longitude of the location.
     * @param apiKey  OpenWeatherMap API key.
     * @param units   Unit system: "metric" (°C), "imperial" (°F), or "standard" (K).
     */
    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
