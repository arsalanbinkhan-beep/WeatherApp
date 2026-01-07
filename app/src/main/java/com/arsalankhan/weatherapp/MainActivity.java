package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private TextView cityText, dateText, tempText, typeText, feelsText;
    private ImageView weatherIcon;
    private ImageButton setting;

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

        // Initialize views
        cityText = findViewById(R.id.citytext);
        dateText = findViewById(R.id.datetext);
        tempText = findViewById(R.id.temptv);
        typeText = findViewById(R.id.typetv);
        feelsText = findViewById(R.id.feelstv);
        weatherIcon = findViewById(R.id.wticon);
        setting = findViewById(R.id.setting);

        // Load weather data
        loadWeatherData();

        // Refresh button click
        ImageButton refreshBtn = findViewById(R.id.setting);
        if (refreshBtn != null) {
            refreshBtn.setOnClickListener(v -> {
                loadWeatherData();
                Toast.makeText(this, "Refreshing weather...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadWeatherData() {
        String city = WeatherUtils.getSavedCity(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<WeatherModels.WeatherResponse> call = service.getCurrentWeather(
                city,
                "metric",
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.WeatherResponse> call, Response<WeatherModels.WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    showError("Failed to load weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.WeatherResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
                // Show sample data
                showSampleData();
            }
        });
    }

    private void updateUI(WeatherModels.WeatherResponse weather) {
        // City and date
        cityText.setText(weather.cityName);
        dateText.setText(WeatherUtils.formatDate(weather.timestamp));

        // Temperature
        double temp = weather.main.temperature;
        double feelsLike = weather.main.feelsLike;

        tempText.setText(WeatherUtils.formatTemperature(temp));
        feelsText.setText("Feels Like " + WeatherUtils.formatTemperature(feelsLike));

        // Weather type
        if (weather.weather.length > 0) {
            String description = weather.weather[0].description;
            typeText.setText(capitalizeWords(description));

            // Load weather icon
            String iconCode = weather.weather[0].icon;
            WeatherUtils.loadWeatherIcon(this, iconCode, weatherIcon);
        }

        // Update humidity and wind in map card
        TextView mapInfo = findViewById(R.id.mapCard).findViewById(TextView.class.getModifiers());
        if (mapInfo != null) {
            String info = "Humidity: " + weather.main.humidity + "%\n" +
                    "Wind: " + weather.wind.speed + " m/s\n" +
                    "Pressure: " + weather.main.pressure + " hPa";
            mapInfo.setText(info);
        }
    }

    private void showSampleData() {
        cityText.setText("Mumbai");
        dateText.setText("Sunday, Dec 2025");
        tempText.setText("15°C");
        typeText.setText("Partially Cloudy");
        feelsText.setText("Feels Like 14°C");
    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private String capitalizeWords(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}