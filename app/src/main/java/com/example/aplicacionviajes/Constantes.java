package com.example.aplicacionviajes;

import com.example.aplicacionviajes.entity.Trip;
import java.util.ArrayList;
import java.util.Random;

public class Constantes {
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
            // Random date in the next year
            long date = System.currentTimeMillis() + (long)(random.nextDouble() * 365L * 24 * 60 * 60 * 1000);
            
            // Using direct IDs from a list of known valid images for better performance and reliability
            int imageId = 10 + i; 
            String imageUrl = "https://picsum.photos/id/" + imageId + "/400/400";
            
            trips.add(new Trip(title, code, city, Math.round(price * 100.0) / 100.0, date, imageUrl));
        }
        return trips;
    }
}
