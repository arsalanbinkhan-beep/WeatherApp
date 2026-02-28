package com.arsalankhan.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    // Current Weather Views
    private TextView cityText, dateText, tempText, typeText, feelsText;
    private TextView aqiText, aqiLevelText;
    private ImageView weatherIcon;
    private ImageButton refreshBtn, locationBtn, favoriteBtn, settingsBtn;

    // Hourly Forecast
    private RecyclerView hourlyRecyclerView;
    private HourlyForecastAdapter hourlyAdapter;

    // Weather Details
    private RecyclerView detailsRecyclerView;
    private WeatherDetailsAdapter detailsAdapter;

    // Loading
    private ShimmerFrameLayout shimmerLayout;
    private View contentLayout;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SEARCH_REQUEST_CODE = 1002;

    // Data
    private WeatherModels.WeatherResponse currentWeather;
    private List<WeatherModels.WeatherDetail> weatherDetails;
    private boolean isFavorite = false;
    private FavoriteCityDao favoriteDao;

    // Auto-refresh
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private static final long AUTO_REFRESH_INTERVAL = 300000; // 5 minutes

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize database
        favoriteDao = WeatherDatabase.getDatabase(this).favoriteCityDao();

        // Initialize all views
        initViews();

        // Setup adapters
        setupAdapters();

        // Setup buttons
        setupButtons();

        // Check auto-refresh settings
        checkAutoRefreshSetting();

        // Load weather data (will use saved city or location)
        loadWeatherData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SEARCH_REQUEST_CODE && resultCode == RESULT_OK) {
            // City was selected in SearchActivity, refresh weather
            loadWeatherData();

            // Show confirmation message
            if (data != null && data.hasExtra("city_name")) {
                String cityName = data.getStringExtra("city_name");
                String country = data.getStringExtra("country");
                Toast.makeText(this, "Now showing: " + cityName + ", " + country,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        // Current weather
        cityText = findViewById(R.id.citytext);
        dateText = findViewById(R.id.datetext);
        tempText = findViewById(R.id.temptv);
        typeText = findViewById(R.id.typetv);
        feelsText = findViewById(R.id.feelstv);
        weatherIcon = findViewById(R.id.wticon);

        // AQI Views
        aqiText = findViewById(R.id.aqiText);
        aqiLevelText = findViewById(R.id.aqiLevelText);

        // Buttons
        refreshBtn = findViewById(R.id.setting);
        locationBtn = findViewById(R.id.locationBtn);
        favoriteBtn = findViewById(R.id.favoriteBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        // RecyclerViews
        hourlyRecyclerView = findViewById(R.id.hourlyRecyclerView);
        detailsRecyclerView = findViewById(R.id.detailsRecyclerView);

        // Loading
        shimmerLayout = findViewById(R.id.shimmerLayout);
        contentLayout = findViewById(R.id.contentLayout);
    }

    private void setupAdapters() {
        // Hourly forecast adapter
        hourlyRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        hourlyAdapter = new HourlyForecastAdapter(new ArrayList<>());
        hourlyRecyclerView.setAdapter(hourlyAdapter);

        // Weather details adapter - Use GridLayoutManager (2 columns) to show more details
        detailsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        weatherDetails = new ArrayList<>();
        detailsAdapter = new WeatherDetailsAdapter(weatherDetails);
        detailsRecyclerView.setAdapter(detailsAdapter);
    }

    private void setupButtons() {
        // Refresh button
        if (refreshBtn != null) {
            refreshBtn.setOnClickListener(v -> {
                // Create rotate animation
                RotateAnimation rotate = new RotateAnimation(
                        0, 360,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                rotate.setDuration(1000);
                refreshBtn.startAnimation(rotate);

                loadWeatherData();
                Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            });
        }

        // Location button
        if (locationBtn != null) {
            locationBtn.setOnClickListener(v -> {
                if (hasLocationPermission()) {
                    loadWeatherByCurrentLocation();
                } else {
                    requestLocationPermission();
                }
            });
        }

        // Favorite button
        if (favoriteBtn != null) {
            favoriteBtn.setOnClickListener(v -> toggleFavorite());
        }

        // Settings button
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> navigateTo(SettingActivity.class));
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            loadWeatherByCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadWeatherByCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Using saved city.", Toast.LENGTH_SHORT).show();
                loadWeatherData();
            }
        }
    }

    private void loadWeatherData() {
        // Check if we should use location or saved city
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        boolean useLocation = prefs.getBoolean("use_location", true);

        if (useLocation && hasLocationPermission()) {
            loadWeatherByCurrentLocation();
        } else {
            loadWeatherForSavedCity();
        }
    }

    private void loadWeatherForSavedCity() {
        showLoading(true);

        String city = WeatherUtils.getSavedCity(this);
        String unit = WeatherUtils.getSavedUnit(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<WeatherModels.WeatherResponse> call = service.getCurrentWeather(
                city,
                unit,
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.WeatherResponse> call,
                                   Response<WeatherModels.WeatherResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentWeather = response.body();
                    updateUI(currentWeather);

                    // Save location
                    WeatherUtils.saveLocation(MainActivity.this,
                            currentWeather.cityName,
                            currentWeather.sys.country,
                            currentWeather.coord.lat,
                            currentWeather.coord.lon);

                    // Load forecast data
                    loadForecastData();

                    // Check if favorite
                    checkIfFavorite();

                    // Load real AQI data
                    loadRealAirQualityData(currentWeather.coord.lat, currentWeather.coord.lon);

                    // Load UV Index and additional data
                    loadAdditionalWeatherData(currentWeather.coord.lat, currentWeather.coord.lon);

                } else {
                    showError("Failed to load weather data");
                    showSampleData();
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.WeatherResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                showSampleData();
            }
        });
    }

    private void loadWeatherByCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }

        showLoading(true);

        LocationRequest locationRequest =
                new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setMinUpdateIntervalMillis(5000)
                        .build();

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        loadWeatherByCoordinates(location.getLatitude(), location.getLongitude());
                        fusedLocationClient.removeLocationUpdates(this);
                        break;
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                loadWeatherByCoordinates(location.getLatitude(), location.getLongitude());
            } else {
                showLoading(false);
                Toast.makeText(this, "Getting location...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWeatherByCoordinates(double lat, double lon) {
        String unit = WeatherUtils.getSavedUnit(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<WeatherModels.WeatherResponse> call = service.getCurrentWeatherByCoords(
                lat,
                lon,
                unit,
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.WeatherResponse> call,
                                   Response<WeatherModels.WeatherResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentWeather = response.body();
                    updateUI(currentWeather);

                    // Save location
                    WeatherUtils.saveLocation(MainActivity.this,
                            currentWeather.cityName,
                            currentWeather.sys.country,
                            currentWeather.coord.lat,
                            currentWeather.coord.lon);

                    // Save preference to use location
                    SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("use_location", true).apply();

                    // Load forecast data
                    loadForecastData();

                    // Check if favorite
                    checkIfFavorite();

                    // Load real AQI data
                    loadRealAirQualityData(currentWeather.coord.lat, currentWeather.coord.lon);

                    // Load UV Index and additional data
                    loadAdditionalWeatherData(currentWeather.coord.lat, currentWeather.coord.lon);

                } else {
                    showError("Failed to load weather data");
                    loadWeatherForSavedCity(); // Fallback to saved city
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.WeatherResponse> call, Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
                loadWeatherForSavedCity(); // Fallback to saved city
            }
        });
    }

    private void loadForecastData() {
        if (currentWeather == null) return;

        String city = currentWeather.cityName;
        String unit = WeatherUtils.getSavedUnit(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<WeatherModels.ForecastResponse> call = service.getForecast(
                city,
                unit,
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.ForecastResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.ForecastResponse> call,
                                   Response<WeatherModels.ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateHourlyForecast(response.body().list);
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.ForecastResponse> call, Throwable t) {
                Log.e("MainActivity", "Forecast error: " + t.getMessage());
            }
        });
    }

    private void loadAdditionalWeatherData(double lat, double lon) {
        String apiKey = WeatherApiService.API_KEY;
        String unit = WeatherUtils.getSavedUnit(this).equals("imperial") ? "imperial" : "metric";
        String oneCallUrl = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat +
                "&lon=" + lon + "&exclude=minutely,daily&appid=" + apiKey +
                "&units=" + unit;

        new Thread(() -> {
            try {
                URL url = new URL(oneCallUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse additional weather data
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

                // Store these values to use in updateWeatherDetailsList
                SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (jsonResponse.has("current")) {
                    JsonObject current = jsonResponse.getAsJsonObject("current");

                    // UV Index
                    if (current.has("uvi")) {
                        editor.putFloat("uvi", (float) current.get("uvi").getAsDouble());
                    } else {
                        editor.putFloat("uvi", 0);
                    }

                    // Dew Point
                    if (current.has("dew_point")) {
                        editor.putFloat("dew_point", (float) current.get("dew_point").getAsDouble());
                    } else {
                        editor.putFloat("dew_point", 0);
                    }

                    // Visibility (in meters)
                    if (current.has("visibility")) {
                        editor.putFloat("visibility_meters", (float) current.get("visibility").getAsDouble());
                    }

                    // Wind Gust
                    if (current.has("wind_gust")) {
                        editor.putFloat("wind_gust", (float) current.get("wind_gust").getAsDouble());
                    }

                    // Cloud percentage
                    if (current.has("clouds")) {
                        editor.putInt("cloud_percentage", current.get("clouds").getAsInt());
                    }

                    // Weather description
                    if (current.has("weather") && current.getAsJsonArray("weather").size() > 0) {
                        JsonObject weather = current.getAsJsonArray("weather").get(0).getAsJsonObject();
                        if (weather.has("description")) {
                            editor.putString("weather_description", weather.get("description").getAsString());
                        }
                    }
                }

                // Apply all changes
                editor.apply();

                runOnUiThread(() -> {
                    // Update the weather details list with new data
                    if (currentWeather != null) {
                        updateWeatherDetailsList(currentWeather);
                    }
                });

            } catch (Exception e) {
                Log.e("AdditionalData", "Error loading additional weather data: " + e.getMessage());
                // Set default values if API call fails
                SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putFloat("uvi", 0)
                        .putFloat("dew_point", 0)
                        .apply();
            }
        }).start();
    }

    private void loadRealAirQualityData(double lat, double lon) {
        String apiKey = WeatherApiService.API_KEY;
        String aqiUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat +
                "&lon=" + lon + "&appid=" + apiKey;

        new Thread(() -> {
            try {
                URL url = new URL(aqiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Parse AQI response
                    Gson gson = new Gson();
                    JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

                    if (jsonResponse.has("list") && jsonResponse.getAsJsonArray("list").size() > 0) {
                        JsonObject aqiData = jsonResponse.getAsJsonArray("list").get(0).getAsJsonObject();
                        JsonObject main = aqiData.getAsJsonObject("main");
                        int europeAqi = main.get("aqi").getAsInt(); // This is 1-5 scale

                        // Convert to US AQI scale and update UI
                        int usAqi = convertToUsAqi(europeAqi);
                        runOnUiThread(() -> updateAirQualityUI(usAqi, europeAqi));
                    } else {
                        runOnUiThread(() -> updateAirQualityUI(-1, -1));
                    }
                } else {
                    runOnUiThread(() -> updateAirQualityUI(-1, -1));
                }

            } catch (Exception e) {
                Log.e("AQI", "Error loading AQI: " + e.getMessage());
                runOnUiThread(() -> updateAirQualityUI(-1, -1));
            }
        }).start();
    }

    // Convert European AQI (1-5) to US AQI (0-500)
    private int convertToUsAqi(int europeAqi) {
        switch (europeAqi) {
            case 1: // Good
                return 25; // 0-50 range
            case 2: // Fair
                return 75; // 51-100 range
            case 3: // Moderate
                return 125; // 101-150 range
            case 4: // Poor
                return 200; // 151-200 range
            case 5: // Very Poor
                return 300; // 201-300 range
            default:
                return 0;
        }
    }

    // Update UI with converted AQI values
    private void updateAirQualityUI(int usAqi, int europeAqi) {
        if (aqiText != null && aqiLevelText != null) {
            if (usAqi == -1) {
                aqiText.setText("AQI: --");
                aqiLevelText.setText("No Data");
                aqiLevelText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                // Display US AQI
                aqiText.setText("AQI: " + usAqi);

                // Get level based on US AQI
                String level = getAQILevel(usAqi);
                aqiLevelText.setText(level);

                // Set color based on US AQI
                int color = getAQIColor(usAqi);
                aqiLevelText.setTextColor(color);

                // Log for debugging
                Log.d("AQI", "Europe AQI: " + europeAqi + " -> US AQI: " + usAqi + " (" + level + ")");
            }
        }
    }

    // Get AQI level based on US AQI scale
    private String getAQILevel(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    // Get color based on US AQI scale
    private int getAQIColor(int aqi) {
        if (aqi <= 50) {
            return getResources().getColor(android.R.color.holo_green_dark); // Good - Green
        } else if (aqi <= 100) {
            return getResources().getColor(android.R.color.holo_orange_light); // Moderate - Yellow/Orange
        } else if (aqi <= 150) {
            return getResources().getColor(android.R.color.holo_orange_dark); // Unhealthy for Sensitive - Orange
        } else if (aqi <= 200) {
            return getResources().getColor(android.R.color.holo_red_light); // Unhealthy - Red
        } else if (aqi <= 300) {
            return getResources().getColor(android.R.color.holo_purple); // Very Unhealthy - Purple
        } else {
            return getResources().getColor(android.R.color.holo_red_dark); // Hazardous - Dark Red
        }
    }

    private void updateUI(WeatherModels.WeatherResponse weather) {
        if (weather == null) return;

        // City and date
        cityText.setText(weather.cityName + ", " + weather.sys.country);
        dateText.setText(WeatherUtils.formatDate(weather.timestamp));

        // Temperature
        String unit = WeatherUtils.getSavedUnit(this);
        double temp = weather.main.temperature;
        double feelsLike = weather.main.feelsLike;

        tempText.setText(WeatherUtils.formatTemperature(temp, unit));
        feelsText.setText("Feels like " + WeatherUtils.formatTemperature(feelsLike, unit));

        // Weather type
        if (weather.weather != null && !weather.weather.isEmpty()) {
            String description = weather.weather.get(0).description;
            typeText.setText(WeatherUtils.capitalizeWords(description));

            // Load weather icon
            String iconCode = weather.weather.get(0).icon;
            WeatherUtils.loadWeatherIcon(this, iconCode, weatherIcon);
        }

        // Update weather details list
        updateWeatherDetailsList(weather);
    }

    private void updateWeatherDetailsList(WeatherModels.WeatherResponse weather) {
        weatherDetails.clear();

        String unit = WeatherUtils.getSavedUnit(this);
        String tempUnit = unit.equals("imperial") ? "°F" : "°C";
        String speedUnit = unit.equals("imperial") ? "mph" : "m/s";

        // Get stored additional weather data
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        float uvi = prefs.getFloat("uvi", 0);
        float dewPoint = prefs.getFloat("dew_point", 0);
        float visibilityMeters = prefs.getFloat("visibility_meters", 0);
        float windGust = prefs.getFloat("wind_gust", 0);
        int cloudPercentage = prefs.getInt("cloud_percentage", -1);
        String weatherDescription = prefs.getString("weather_description", "");

        // --- COLUMN 1: BASIC WEATHER DETAILS ---

        // 1. Temperature
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Temperature",
                WeatherUtils.formatTemperature(weather.main.temperature, unit),
                tempUnit,
                R.drawable.ic_thermometer
        ));

        // 2. Feels Like
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Feels Like",
                WeatherUtils.formatTemperature(weather.main.feelsLike, unit),
                tempUnit,
                R.drawable.ic_feels_like
        ));

        // 3. Min Temperature
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Min Temp",
                WeatherUtils.formatTemperature(weather.main.tempMin, unit),
                tempUnit,
                R.drawable.ic_temp_min
        ));

        // 4. Max Temperature
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Max Temp",
                WeatherUtils.formatTemperature(weather.main.tempMax, unit),
                tempUnit,
                R.drawable.ic_temp_max
        ));

        // 5. Humidity
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Humidity",
                String.valueOf(weather.main.humidity),
                "%",
                R.drawable.ic_humidity
        ));

        // 6. Pressure
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Pressure",
                String.valueOf(weather.main.pressure),
                "hPa",
                R.drawable.ic_pressure
        ));

        // 7. Wind Speed
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Wind Speed",
                String.format(Locale.getDefault(), "%.1f", weather.wind.speed),
                speedUnit,
                R.drawable.ic_wind
        ));

        // 8. Wind Direction
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Wind Direction",
                WeatherUtils.getWindDirection(weather.wind.degree),
                weather.wind.degree + "°",
                R.drawable.ic_wind_direction
        ));

        // --- COLUMN 2: ADVANCED WEATHER DETAILS ---

        // 9. Wind Gust
        if (windGust > 0) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Wind Gust",
                    String.format(Locale.getDefault(), "%.1f", windGust),
                    speedUnit,
                    R.drawable.ic_wind_gust
            ));
        } else if (weather.wind.gust > 0) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Wind Gust",
                    String.format(Locale.getDefault(), "%.1f", weather.wind.gust),
                    speedUnit,
                    R.drawable.ic_wind_gust
            ));
        } else {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Wind Gust",
                    "N/A",
                    speedUnit,
                    R.drawable.ic_wind_gust
            ));
        }

        // 10. Visibility
        float visibilityKm = 0;
        if (visibilityMeters > 0) {
            visibilityKm = visibilityMeters / 1000f;
        } else if (weather.visibility > 0) {
            visibilityKm = weather.visibility / 1000f;
        }

        if (visibilityKm > 0) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Visibility",
                    String.format(Locale.getDefault(), "%.1f", visibilityKm),
                    "km",
                    R.drawable.ic_visibility
            ));
        } else {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Visibility",
                    "N/A",
                    "",
                    R.drawable.ic_visibility
            ));
        }

        // 11. Cloud Cover
        if (cloudPercentage >= 0) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Cloud Cover",
                    String.valueOf(cloudPercentage),
                    "%",
                    R.drawable.ic_cloud
            ));
        } else if (weather.clouds != null) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Cloud Cover",
                    String.valueOf(weather.clouds.all),
                    "%",
                    R.drawable.ic_cloud
            ));
        } else {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Cloud Cover",
                    "N/A",
                    "%",
                    R.drawable.ic_cloud
            ));
        }

        // 12. UV Index
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "UV Index",
                String.format(Locale.getDefault(), "%.1f", uvi),
                getUVILevel(uvi),
                R.drawable.ic_uv_index
        ));

        // 13. Dew Point
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Dew Point",
                formatDewPoint(dewPoint, unit),
                tempUnit,
                R.drawable.ic_dew_point
        ));

        // 14. Heat Index
        String heatIndex = calculateHeatIndex(weather.main.temperature, weather.main.humidity, unit);
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Heat Index",
                heatIndex,
                tempUnit,
                R.drawable.ic_heat
        ));

        // 15. Wind Chill
        String windChill = calculateWindChill(weather.main.temperature, weather.wind.speed, unit);
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Wind Chill",
                windChill,
                tempUnit,
                R.drawable.ic_wind_chill
        ));

        // 16. Apparent Temperature
        String apparentTemp = calculateApparentTemperature(weather.main.temperature, weather.wind.speed,
                weather.main.humidity, unit);
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Apparent Temp",
                apparentTemp,
                tempUnit,
                R.drawable.ic_apparent_temp
        ));

        // 17. Sunrise
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Sunrise",
                WeatherUtils.formatTime(weather.sys.sunrise),
                "",
                R.drawable.ic_sunrise
        ));

        // 18. Sunset
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Sunset",
                WeatherUtils.formatTime(weather.sys.sunset),
                "",
                R.drawable.ic_sunset
        ));

        // 19. Weather Condition
        String condition = "";
        if (!weatherDescription.isEmpty()) {
            condition = WeatherUtils.capitalizeWords(weatherDescription);
        } else if (weather.weather != null && !weather.weather.isEmpty()) {
            condition = weather.weather.get(0).main;
        }

        if (!condition.isEmpty()) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Condition",
                    condition,
                    "",
                    R.drawable.ic_weather
            ));
        }

        // 20. Weather Description
        if (weather.weather != null && !weather.weather.isEmpty()) {
            weatherDetails.add(new WeatherModels.WeatherDetail(
                    "Description",
                    WeatherUtils.capitalizeWords(weather.weather.get(0).description),
                    "",
                    R.drawable.ic_description
            ));
        }

        // 21. Atmospheric Pressure Trend (Simulated)
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Pressure Trend",
                getPressureTrend(weather.main.pressure),
                "",
                R.drawable.ic_trend
        ));

        // 22. Humidity Level Status
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Humidity Status",
                getHumidityStatus(weather.main.humidity),
                "",
                R.drawable.ic_humidity_level
        ));

        // 23. Comfort Level
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Comfort Level",
                getComfortLevel(weather.main.temperature, weather.main.humidity, unit),
                "",
                R.drawable.ic_comfort
        ));

        // 24. Precipitation Chance (Simulated - would need forecast data)
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Precipitation",
                calculatePrecipitationChance(weather.weather),
                "%",
                R.drawable.ic_precipitation
        ));

        detailsAdapter.notifyDataSetChanged();
    }

    private String getWindDirection(double degrees) {
        if (degrees >= 337.5 || degrees < 22.5) return "N";
        if (degrees >= 22.5 && degrees < 67.5) return "NE";
        if (degrees >= 67.5 && degrees < 112.5) return "E";
        if (degrees >= 112.5 && degrees < 157.5) return "SE";
        if (degrees >= 157.5 && degrees < 202.5) return "S";
        if (degrees >= 202.5 && degrees < 247.5) return "SW";
        if (degrees >= 247.5 && degrees < 292.5) return "W";
        return "NW";
    }

    private String getUVILevel(double uvi) {
        if (uvi <= 2) return "Low";
        if (uvi <= 5) return "Moderate";
        if (uvi <= 7) return "High";
        if (uvi <= 10) return "Very High";
        return "Extreme";
    }

    private String formatDewPoint(double dewPoint, String unit) {
        if (unit.equals("imperial")) {
            dewPoint = (dewPoint * 9/5) + 32;
        }
        return String.format(Locale.getDefault(), "%.1f", dewPoint);
    }

    private String calculateHeatIndex(double temp, double humidity, String unit) {
        if (unit.equals("metric")) {
            temp = (temp * 9/5) + 32;
        }

        if (temp < 80) {
            return "N/A";
        }

        double heatIndex = -42.379 + 2.04901523 * temp + 10.14333127 * humidity
                - 0.22475541 * temp * humidity - 6.83783e-3 * temp * temp
                - 5.481717e-2 * humidity * humidity + 1.22874e-3 * temp * temp * humidity
                + 8.5282e-4 * temp * humidity * humidity - 1.99e-6 * temp * temp * humidity * humidity;

        if (unit.equals("metric")) {
            heatIndex = (heatIndex - 32) * 5/9;
        }

        return String.format(Locale.getDefault(), "%.1f", heatIndex);
    }

    private String calculateWindChill(double temp, double windSpeed, String unit) {
        if (unit.equals("metric")) {
            temp = (temp * 9/5) + 32;
            windSpeed = windSpeed * 2.23694;
        }

        if (temp > 50 || windSpeed <= 3) {
            return "N/A";
        }

        double windChill = 35.74 + 0.6215 * temp - 35.75 * Math.pow(windSpeed, 0.16)
                + 0.4275 * temp * Math.pow(windSpeed, 0.16);

        if (unit.equals("metric")) {
            windChill = (windChill - 32) * 5/9;
        }

        return String.format(Locale.getDefault(), "%.1f", windChill);
    }

    private String calculateApparentTemperature(double temp, double windSpeed,
                                                double humidity, String unit) {
        if (unit.equals("metric")) {
            temp = (temp * 9/5) + 32;
            windSpeed = windSpeed * 2.23694;
        }

        double apparentTemp;

        if (temp <= 50 && windSpeed > 3) {
            apparentTemp = 35.74 + 0.6215 * temp - 35.75 * Math.pow(windSpeed, 0.16)
                    + 0.4275 * temp * Math.pow(windSpeed, 0.16);
        } else if (temp >= 80) {
            apparentTemp = -42.379 + 2.04901523 * temp + 10.14333127 * humidity
                    - 0.22475541 * temp * humidity - 6.83783e-3 * temp * temp
                    - 5.481717e-2 * humidity * humidity + 1.22874e-3 * temp * temp * humidity
                    + 8.5282e-4 * temp * humidity * humidity - 1.99e-6 * temp * temp * humidity * humidity;
        } else {
            apparentTemp = temp;
        }

        if (unit.equals("metric")) {
            apparentTemp = (apparentTemp - 32) * 5/9;
        }

        return String.format(Locale.getDefault(), "%.1f", apparentTemp);
    }

    private String getPressureTrend(int pressure) {
        // Simulated pressure trend (in real app, you'd track pressure over time)
        if (pressure > 1013) return "Rising";
        if (pressure < 1013) return "Falling";
        return "Steady";
    }

    private String getHumidityStatus(int humidity) {
        if (humidity < 30) return "Dry";
        if (humidity < 50) return "Comfortable";
        if (humidity < 70) return "Moderate";
        return "Humid";
    }

    private String getComfortLevel(double temp, int humidity, String unit) {
        if (unit.equals("metric")) {
            temp = (temp * 9/5) + 32;
        }

        if (temp < 50) return "Cold";
        if (temp < 68) return "Cool";
        if (temp < 77) {
            if (humidity < 50) return "Comfortable";
            return "Mild";
        }
        if (temp < 86) {
            if (humidity < 60) return "Warm";
            return "Hot";
        }
        return "Very Hot";
    }

    private String calculatePrecipitationChance(List<WeatherModels.Weather> weatherList) {
        if (weatherList == null || weatherList.isEmpty()) return "0";

        String main = weatherList.get(0).main.toLowerCase();
        if (main.contains("rain") || main.contains("drizzle")) return "80";
        if (main.contains("snow")) return "60";
        if (main.contains("thunderstorm")) return "90";
        if (main.contains("mist") || main.contains("fog")) return "30";
        if (main.contains("clouds")) return "20";
        return "10";
    }

    private void updateHourlyForecast(List<WeatherModels.ForecastItem> forecastItems) {
        if (forecastItems == null || forecastItems.isEmpty()) return;

        List<WeatherModels.ForecastItem> hourlyItems = new ArrayList<>();

        int count = Math.min(forecastItems.size(), 12);
        for (int i = 0; i < count; i++) {
            hourlyItems.add(forecastItems.get(i));
        }

        hourlyAdapter.updateData(hourlyItems);
    }

    private void checkIfFavorite() {
        if (currentWeather == null) return;

        new Thread(() -> {
            FavoriteCity favorite = favoriteDao.findByNameAndCountry(
                    currentWeather.cityName,
                    currentWeather.sys.country
            );

            runOnUiThread(() -> {
                isFavorite = (favorite != null);
                updateFavoriteButton();
            });
        }).start();
    }

    private void toggleFavorite() {
        if (currentWeather == null) return;

        new Thread(() -> {
            if (isFavorite) {
                FavoriteCity favorite = favoriteDao.findByNameAndCountry(
                        currentWeather.cityName,
                        currentWeather.sys.country
                );
                if (favorite != null) {
                    favoriteDao.delete(favorite);
                }
            } else {
                FavoriteCity favorite = new FavoriteCity(
                        currentWeather.cityName,
                        currentWeather.sys.country,
                        "",
                        currentWeather.coord.lat,
                        currentWeather.coord.lon
                );
                favoriteDao.insert(favorite);
            }

            isFavorite = !isFavorite;

            runOnUiThread(() -> {
                updateFavoriteButton();
                Toast.makeText(MainActivity.this,
                        isFavorite ? "Added to favorites" : "Removed from favorites",
                        Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void updateFavoriteButton() {
        if (favoriteBtn != null) {
            if (isFavorite) {
                favoriteBtn.setImageResource(R.drawable.ic_favorite_filled);
                favoriteBtn.setContentDescription("Remove from favorites");
            } else {
                favoriteBtn.setImageResource(R.drawable.ic_favorite_border);
                favoriteBtn.setContentDescription("Add to favorites");
            }
        }
    }

    private void checkAutoRefreshSetting() {
        boolean autoRefreshEnabled = getSharedPreferences("WeatherPrefs", MODE_PRIVATE)
                .getBoolean("auto_refresh", false);

        if (autoRefreshEnabled) {
            startAutoRefresh();
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshHandler == null) {
            autoRefreshHandler = new Handler(Looper.getMainLooper());
        }

        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadWeatherData();
                autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
            }
        };

        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
    }

    private void stopAutoRefresh() {
        if (autoRefreshHandler != null && autoRefreshRunnable != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
    }

    private void showLoading(boolean show) {
        if (shimmerLayout != null && contentLayout != null) {
            if (show) {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmer();
                contentLayout.setVisibility(View.GONE);
            } else {
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> loadWeatherData())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSampleData() {
        cityText.setText("Mumbai, IN");
        dateText.setText(WeatherUtils.formatDate(System.currentTimeMillis() / 1000));
        tempText.setText("--°C");
        typeText.setText("--");
        feelsText.setText("Feels like --°C");

        weatherIcon.setImageResource(R.drawable.ic_weather_cloud_sun);

        // Show sample AQI with proper conversion
        int sampleEuropeAqi = 2; // Fair
        int sampleUsAqi = convertToUsAqi(sampleEuropeAqi);
        updateAirQualityUI(sampleUsAqi, sampleEuropeAqi);

        // Clear sample details
        weatherDetails.clear();
        weatherDetails.add(new WeatherModels.WeatherDetail("Temperature", "--", "°C", R.drawable.ic_thermometer));
        weatherDetails.add(new WeatherModels.WeatherDetail("Humidity", "--", "%", R.drawable.ic_humidity));
        weatherDetails.add(new WeatherModels.WeatherDetail("Wind Speed", "--", "m/s", R.drawable.ic_wind));
        weatherDetails.add(new WeatherModels.WeatherDetail("Pressure", "--", "hPa", R.drawable.ic_pressure));
        detailsAdapter.notifyDataSetChanged();
    }
}