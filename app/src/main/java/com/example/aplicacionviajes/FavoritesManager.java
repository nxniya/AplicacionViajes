package com.example.aplicacionviajes;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.aplicacionviajes.entity.Trip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesManager {

    private static final String PREFS_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorite_codes";

    public static void saveFavoriteCode(Context context, String code, boolean isFavorite) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favorites = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
        if (isFavorite) {
            favorites.add(code);
        } else {
            favorites.remove(code);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public static void applyFavorites(Context context, List<Trip> trips) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favCodes = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        for (Trip trip : trips) {
            trip.setFavorite(favCodes.contains(trip.getCodigo()));
        }
    }
}
