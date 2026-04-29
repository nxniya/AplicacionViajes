package com.example.aplicacionviajes;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aplicacionviajes.entity.Trip;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseDatabaseService {

    private static final String TAG = "FirebaseDatabaseService";
    private static boolean persistenceEnabled = false;

    // ─── Callbacks ─────────────────────────────────────────────────────────────

    public interface TripCallback {
        void onSuccess(DataSnapshot snapshot);
        void onFailure(DatabaseError error);
    }

    public interface TripsCallback {
        void onSuccess(ArrayList<Trip> trips);
        void onFailure(DatabaseError error);
    }

    // ─── References ────────────────────────────────────────────────────────────

    private final String userId;
    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference databaseReference; // users/{userId}/trips
    private final DatabaseReference tripsRef;           // global /trips

    // ─── Active listeners (for lifecycle management) ────────────────────────────

    private ChildEventListener childEventListener;
    private ValueEventListener valueEventListener;

    // ─── Constructor ───────────────────────────────────────────────────────────

    public FirebaseDatabaseService(String userId) {
        if (!persistenceEnabled) {
            FirebaseDatabase.getInstance("https://appviajes-ec7f8-default-rtdb.europe-west1.firebasedatabase.app").setPersistenceEnabled(true);
            persistenceEnabled = true;
        }
        this.userId = userId;
        this.firebaseDatabase = FirebaseDatabase.getInstance("https://appviajes-ec7f8-default-rtdb.europe-west1.firebasedatabase.app");
        this.databaseReference = firebaseDatabase.getReference("users").child(userId).child("trips");
        this.tripsRef = firebaseDatabase.getReference("trips");
    }

    // ─── Global trips queries ───────────────────────────────────────────────────

    /** Load all trips without any filter. */
    public void getAllTrips(TripsCallback callback) {
        tripsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Viajes cargados: " + snapshot.getChildrenCount());
                callback.onSuccess(parseTrips(snapshot));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al cargar viajes: " + error.getMessage());
                callback.onFailure(error);
            }
        });
    }

    /** Filter trips by exact city name (Firebase server-side query). */
    public void getTripsByCity(String city, TripsCallback callback) {
        tripsRef.orderByChild("ciudad").equalTo(city)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "Viajes por ciudad '" + city + "': " + snapshot.getChildrenCount());
                        callback.onSuccess(parseTrips(snapshot));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error al filtrar por ciudad: " + error.getMessage());
                        callback.onFailure(error);
                    }
                });
    }

    /** Filter trips within a price range (Firebase server-side query). */
    public void getTripsByPriceRange(double min, double max, TripsCallback callback) {
        tripsRef.orderByChild("precio").startAt(min).endAt(max)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "Viajes por precio " + min + "-" + max + ": " + snapshot.getChildrenCount());
                        callback.onSuccess(parseTrips(snapshot));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error al filtrar por precio: " + error.getMessage());
                        callback.onFailure(error);
                    }
                });
    }

    /** Filter trips within a date range (Firebase server-side query). */
    public void getTripsByDateRange(long min, long max, TripsCallback callback) {
        tripsRef.orderByChild("fecha").startAt((double) min).endAt((double) max)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d(TAG, "Viajes por fecha: " + snapshot.getChildrenCount());
                        callback.onSuccess(parseTrips(snapshot));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error al filtrar por fecha: " + error.getMessage());
                        callback.onFailure(error);
                    }
                });
    }

    /** Seeds the global /trips node only if it is empty. Calls onDone when finished. */
    /**
     * @param onSeeded called only when new trips are actually written (first run / empty DB).
     *                 Use this to clear stale SharedPreferences favorites.
     * @param onDone   always called when the check finishes (success or error).
     */
    public void initTripsIfEmpty(ArrayList<Trip> trips, Runnable onSeeded, Runnable onDone) {
        tripsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    for (Trip trip : trips) {
                        tripsRef.push().setValue(trip);
                    }
                    Log.d(TAG, "Base de datos inicializada con " + trips.size() + " viajes");
                    if (onSeeded != null) onSeeded.run();
                } else {
                    Log.d(TAG, "La base de datos ya contiene viajes, no se reinicializa");
                }
                if (onDone != null) onDone.run();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al inicializar viajes: " + error.getMessage());
                if (onDone != null) onDone.run();
            }
        });
    }

    // ─── Per-user trip operations ───────────────────────────────────────────────

    public void getTravelById(String tripId, TripCallback callback) {
        databaseReference.child(tripId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        callback.onSuccess(snapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error);
                    }
                });
    }

    public void saveTrip(Trip trip, DatabaseReference ref) {
        ref.push().setValue(trip, (error, dbRef) -> {
            if (error != null) {
                Log.e(TAG, "Error al guardar el viaje: " + error.getMessage());
            } else {
                Log.d(TAG, "Viaje guardado correctamente con clave: " + dbRef.getKey());
            }
        });
    }

    // ─── Real-time listeners ────────────────────────────────────────────────────

    /** Attaches a ChildEventListener to /users/{uid}/trips. No-op if already attached. */
    public void getTrips() {
        if (childEventListener != null) return;
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "Viaje añadido: " + snapshot.getKey());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "Viaje modificado: " + snapshot.getKey());
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Viaje eliminado: " + snapshot.getKey());
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d(TAG, "Viaje movido: " + snapshot.getKey());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al escuchar viajes: " + error.getMessage());
            }
        };
        databaseReference.addChildEventListener(childEventListener);
    }

    /** Attaches a ValueEventListener to /users/{uid}/trips. No-op if already attached. */
    public void listenToTrips() {
        if (valueEventListener != null) return;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Cambio en los viajes: " + snapshot.getChildrenCount() + " viaje(s) en total");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al escuchar cambios en los viajes: " + error.getMessage());
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    /** Removes all active listeners. Call from Activity.onPause(). */
    public void onPause() {
        if (childEventListener != null) {
            databaseReference.removeEventListener(childEventListener);
            childEventListener = null;
            Log.d(TAG, "ChildEventListener desuscrito correctamente");
        }
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
            valueEventListener = null;
            Log.d(TAG, "ValueEventListener desuscrito correctamente");
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private ArrayList<Trip> parseTrips(DataSnapshot snapshot) {
        ArrayList<Trip> trips = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            Trip trip = child.getValue(Trip.class);
            if (trip != null) {
                trip.setFirebaseKey(child.getKey());
                trips.add(trip);
            }
        }
        return trips;
    }
}
