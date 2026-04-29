package com.example.aplicacionviajes;

import com.example.aplicacionviajes.entity.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Constantes {

    private static final Map<String, double[]> CITY_COORDS = new HashMap<>();

    static {
        CITY_COORDS.put("Madrid",      new double[]{ 40.4168,  -3.7038 });
        CITY_COORDS.put("París",       new double[]{ 48.8566,   2.3522 });
        CITY_COORDS.put("Londres",     new double[]{ 51.5074,  -0.1278 });
        CITY_COORDS.put("Roma",        new double[]{ 41.9028,  12.4964 });
        CITY_COORDS.put("Berlín",      new double[]{ 52.5200,  13.4050 });
        CITY_COORDS.put("Tokio",       new double[]{ 35.6762, 139.6503 });
        CITY_COORDS.put("Nueva York",  new double[]{ 40.7128, -74.0060 });
        CITY_COORDS.put("Sídney",      new double[]{-33.8688, 151.2093 });
        CITY_COORDS.put("Barcelona",   new double[]{ 41.3851,   2.1734 });
        CITY_COORDS.put("Ámsterdam",   new double[]{ 52.3676,   4.9041 });
    }

    /** Returns the known coordinates for a city name, or null if not found. */
    public static double[] getCoordsForCity(String city) {
        return CITY_COORDS.get(city);
    }

    public static ArrayList<Trip> generateRandomTrips(int count) {
        String[] titles = {"Escapada", "Aventura", "Tour", "Visita", "Viaje", "Descubrimiento", "Ruta", "Expedición", "Crucero", "Safari"};
        String[] cities = {"Madrid", "París", "Londres", "Roma", "Berlín", "Tokio", "Nueva York", "Sídney", "Barcelona", "Ámsterdam"};
        ArrayList<Trip> trips = new ArrayList<>();
        Random random = new Random(42); // Fixed seed so trips are stable across app restarts

        for (int i = 0; i < count; i++) {
            String city = cities[random.nextInt(cities.length)];
            String title = titles[random.nextInt(titles.length)] + " en " + city;
            String code = city.substring(0, 3).toUpperCase() + String.format("%03d", i);
            double price = 50 + (1000 - 50) * random.nextDouble();
            long date = System.currentTimeMillis() + (long)(random.nextDouble() * 365L * 24 * 60 * 60 * 1000);

            int imageId = 10 + i;
            String imageUrl = "https://picsum.photos/id/" + imageId + "/400/400";

            double[] coords = CITY_COORDS.getOrDefault(city, new double[]{0.0, 0.0});
            trips.add(new Trip(title, code, city, Math.round(price * 100.0) / 100.0, date, imageUrl, coords[0], coords[1]));
        }
        return trips;
    }
}

