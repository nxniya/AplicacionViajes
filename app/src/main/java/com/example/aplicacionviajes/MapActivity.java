package com.example.aplicacionviajes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.aplicacionviajes.entity.Trip;
import com.example.aplicacionviajes.restTypes.WeatherResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String WEATHER_API_KEY = "14033f7e6a27cee5b15b442267e5c957";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Marker userLocationMarker;

    private Trip trip;
    private TextView tvWeatherCity, tvWeatherTemp, tvWeatherDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        trip = getIntent().getParcelableExtra("trip");

        tvWeatherCity = findViewById(R.id.tvWeatherCity);
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp);
        tvWeatherDesc = findViewById(R.id.tvWeatherDesc);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result.getLastLocation() != null) {
                    LatLng userLatLng = new LatLng(
                            result.getLastLocation().getLatitude(),
                            result.getLastLocation().getLongitude()
                    );
                    updateUserMarker(userLatLng);
                }
            }
        };

        if (trip != null && (trip.getLatitude() != 0 || trip.getLongitude() != 0)) {
            fetchWeather(trip.getLatitude(), trip.getLongitude());
        }
        // If coords are 0, weather will be fetched after geocoding in onMapReady
    }

    // ─── OnMapReadyCallback ─────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        if (trip != null) {
            double lat = trip.getLatitude();
            double lon = trip.getLongitude();

            if (lat == 0 && lon == 0 && trip.getCiudad() != null) {
                // 1. Try hardcoded coords from Constantes (works offline/emulator)
                double[] knownCoords = Constantes.getCoordsForCity(trip.getCiudad());
                if (knownCoords != null) {
                    placeDestinationMarker(knownCoords[0], knownCoords[1]);
                    fetchWeather(knownCoords[0], knownCoords[1]);
                } else {
                    // 2. Fallback: Geocoder on background thread
                    new Thread(() -> {
                        try {
                            Geocoder geocoder = new Geocoder(this);
                            java.util.List<Address> addresses = geocoder.getFromLocationName(trip.getCiudad(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                double gLat = addresses.get(0).getLatitude();
                                double gLon = addresses.get(0).getLongitude();
                                runOnUiThread(() -> placeDestinationMarker(gLat, gLon));
                                runOnUiThread(() -> fetchWeather(gLat, gLon));
                            } else {
                                Log.w(TAG, "Geocoder no encontró: " + trip.getCiudad());
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error en geocoder: " + e.getMessage());
                        }
                    }).start();
                }
            } else if (lat != 0 || lon != 0) {
                placeDestinationMarker(lat, lon);
            }
        }

        requestLocationPermission();
    }

    private void placeDestinationMarker(double lat, double lon) {
        if (googleMap == null) return;
        LatLng destination = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions()
                .position(destination)
                .title(trip != null ? trip.getTitulo() : "Destino")
                .snippet(trip != null ? trip.getCiudad() : "")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 12f));
    }

    // ─── Location permission & updates ─────────────────────────────────────────

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1500)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void updateUserMarker(LatLng latLng) {
        if (googleMap == null) return;
        if (userLocationMarker != null) {
            userLocationMarker.remove();
        }
        userLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Tu ubicación")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    // ─── Retrofit: OpenWeatherMap ───────────────────────────────────────────────

    private void fetchWeather(double lat, double lon) {
        RetrofitClient.getWeatherService()
                .getWeatherByCoords(lat, lon, WEATHER_API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse weather = response.body();
                            String city = weather.getName() != null ? weather.getName() : "";
                            double temp = weather.getMain() != null ? weather.getMain().getTemp() : 0;
                            String desc = (weather.getWeather() != null && !weather.getWeather().isEmpty())
                                    ? weather.getWeather().get(0).getDescription() : "";
                            if (!desc.isEmpty()) {
                                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                            }
                            tvWeatherCity.setText("Destino: " + city);
                            tvWeatherTemp.setText(String.format("%.1f°C", temp));
                            tvWeatherDesc.setText(desc);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error al obtener el tiempo: " + t.getMessage());
                        tvWeatherCity.setText("Tiempo no disponible");
                    }
                });
    }

    // ─── Lifecycle ──────────────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
