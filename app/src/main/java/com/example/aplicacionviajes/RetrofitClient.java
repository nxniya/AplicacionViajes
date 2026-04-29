package com.example.aplicacionviajes;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.openweathermap.org/";

    private static Retrofit instance;

    private RetrofitClient() {}

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static WeatherApiService getWeatherService() {
        return getInstance().create(WeatherApiService.class);
    }
}
