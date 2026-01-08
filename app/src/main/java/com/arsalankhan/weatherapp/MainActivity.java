package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    // Current Weather Views
    private TextView cityText, dateText, tempText, typeText, feelsText;
    private TextView aqiText, aqiLevelText; // AQI Views
    private ImageView weatherIcon;
    private ImageButton refreshBtn, locationBtn, favoriteBtn;

    // Hourly Forecast
    private RecyclerView hourlyRecyclerView;
    private HourlyForecastAdapter hourlyAdapter;

    // Weather Details
    private RecyclerView detailsRecyclerView;
    private WeatherDetailsAdapter detailsAdapter;

    // Loading
    private ShimmerFrameLayout shimmerLayout;
    private View contentLayout;

    // Data
    private WeatherModels.WeatherResponse currentWeather;
    private List<WeatherModels.WeatherDetail> weatherDetails;
    private boolean isFavorite = false;

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

        // Initialize all views
        initViews();

        // Setup adapters
        setupAdapters();

        // Setup buttons
        setupButtons();

        // Load weather data
        loadWeatherData();
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

        // Weather details adapter
        detailsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                // Request location permission and get current location
                getCurrentLocationWeather();
            });
        }

        // Favorite button
        if (favoriteBtn != null) {
            favoriteBtn.setOnClickListener(v -> {
                toggleFavorite();
            });
        }
    }

    private void loadWeatherData() {
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

    private void loadForecastData() {
        String city = WeatherUtils.getSavedCity(this);
        String unit = WeatherUtils.getSavedUnit(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        // Fix: Use correct method signature (3 parameters)
        Call<WeatherModels.ForecastResponse> call = service.getForecast(
                city,
                unit,
                WeatherApiService.API_KEY  // Only 3 parameters!
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
                // Silent fail for forecast
            }
        });
    }

    private void updateUI(WeatherModels.WeatherResponse weather) {
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

        // Add all weather details
        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Humidity",
                String.valueOf(weather.main.humidity),
                "%",
                R.drawable.ic_humidity
        ));

        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Wind Speed",
                String.format(Locale.getDefault(), "%.1f", weather.wind.speed),
                "m/s",
                R.drawable.ic_wind
        ));

        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Pressure",
                String.valueOf(weather.main.pressure),
                "hPa",
                R.drawable.ic_pressure
        ));

        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Visibility",
                String.valueOf(weather.visibility / 1000),
                "km",
                R.drawable.ic_visibility
        ));

        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Feels Like",
                WeatherUtils.formatTemperature(weather.main.feelsLike, unit),
                "",
                R.drawable.ic_thermometer
        ));

        weatherDetails.add(new WeatherModels.WeatherDetail(
                "Min/Max Temp",
                WeatherUtils.formatTemperature(weather.main.tempMin, unit) + "/" +
                        WeatherUtils.formatTemperature(weather.main.tempMax, unit),
                "",
                R.drawable.ic_temp_range
        ));

        detailsAdapter.notifyDataSetChanged();
    }

    private void updateHourlyForecast(List<WeatherModels.ForecastItem> forecastItems) {
        List<WeatherModels.ForecastItem> hourlyItems = new ArrayList<>();

        // Take next 8 hours (or available items)
        int count = Math.min(forecastItems.size(), 8);
        for (int i = 0; i < count; i++) {
            hourlyItems.add(forecastItems.get(i));
        }

        hourlyAdapter.updateData(hourlyItems);
    }

    private void updateAirQualityUI(int aqi) {
        // Update air quality in UI
        if (aqiText != null && aqiLevelText != null) {
            aqiText.setText("AQI: " + aqi);
            aqiLevelText.setText(WeatherUtils.getAQILevel(aqi));
            aqiLevelText.setTextColor(WeatherUtils.getAQIColor(this, aqi));
        }
    }

    private void getCurrentLocationWeather() {
        // Implement location permission and fetching
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
        // For now, just refresh with current city
        loadWeatherData();
    }

    private void checkIfFavorite() {
        if (currentWeather == null) return;

        new Thread(() -> {
            // Simplified - skip database for now
            runOnUiThread(() -> {
                isFavorite = false;
                updateFavoriteButton();
            });
        }).start();
    }

    private void toggleFavorite() {
        if (currentWeather == null) return;

        isFavorite = !isFavorite;
        updateFavoriteButton();

        Toast.makeText(this,
                isFavorite ? "Added to favorites" : "Removed from favorites",
                Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteButton() {
        if (favoriteBtn != null) {
            if (isFavorite) {
                favoriteBtn.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                favoriteBtn.setImageResource(R.drawable.ic_favorite_border);
            }
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
        dateText.setText("Sunday, Dec 2025");
        tempText.setText("15°C");
        typeText.setText("Partially Cloudy");
        feelsText.setText("Feels Like 14°C");

        // Load default icon
        weatherIcon.setImageResource(R.drawable.ic_weather_cloud_sun);

        // Sample AQI
        updateAirQualityUI(60);
    }
}