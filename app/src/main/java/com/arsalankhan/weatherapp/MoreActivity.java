package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

public class MoreActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_more;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MoreActivity", "MoreActivity created");

        setupMenuItems();
    }

    private void setupMenuItems() {
        Log.d("MoreActivity", "Setting up menu items");

        // FIXED: Changed LinearLayout to CardView
        // Settings - Navigate to SettingActivity
        CardView settingsItem = findViewById(R.id.settingsItem);
        if (settingsItem != null) {
            settingsItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "Settings clicked");
                navigateTo(SettingActivity.class);
            });
        } else {
            Log.e("MoreActivity", "Settings item not found!");
        }

        // Favorites - Navigate to SearchActivity which shows favorites
        CardView favoritesItem = findViewById(R.id.favoritesItem);
        if (favoritesItem != null) {
            favoritesItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "Favorites clicked");
                // Navigate to SearchActivity (which shows favorites when search is empty)
                navigateTo(SearchActivity.class);
            });
        }

        // Weather Map - Open browser
        CardView weatherMapItem = findViewById(R.id.weatherMapItem);
        if (weatherMapItem != null) {
            weatherMapItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "Weather Map clicked");
                openWeatherMap();
            });
        }

        // AQI Map - Navigate to AqiMapActivity
        CardView aqiMapItem = findViewById(R.id.aqiMapItem);
        if (aqiMapItem != null) {
            aqiMapItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "AQI Map clicked");
                navigateTo(AqiMapActivity.class);
            });
        }

        // Air Quality - Show info dialog
        CardView airQualityItem = findViewById(R.id.airQualityItem);
        if (airQualityItem != null) {
            airQualityItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "Air Quality clicked");
                showAirQualityInfo();
            });
        }

        // UV Index - Show info dialog
        CardView uvIndexItem = findViewById(R.id.uvIndexItem);
        if (uvIndexItem != null) {
            uvIndexItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "UV Index clicked");
                showUVIndexInfo();
            });
        }

        // About - Show about dialog
        CardView aboutItem = findViewById(R.id.aboutItem);
        if (aboutItem != null) {
            aboutItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "About clicked");
                showAboutDialog();
            });
        }

        // Share App - Share via intent
        CardView shareItem = findViewById(R.id.shareItem);
        if (shareItem != null) {
            shareItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "Share clicked");
                shareApp();
            });
        }

        // Rate App - Open Play Store
        CardView rateItem = findViewById(R.id.rateItem);
        if (rateItem != null) {
            rateItem.setOnClickListener(v -> {
                Log.d("MoreActivity", "Rate clicked");
                rateApp();
            });
        }

        Log.d("MoreActivity", "Menu items setup complete");
    }

    private void openWeatherMap() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://openweathermap.org/weathermap"));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open browser", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAirQualityInfo() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Air Quality Information")
                .setMessage("Air Quality Index (AQI) levels:\n\n" +
                        "• Good (0-50): Air quality is satisfactory\n" +
                        "• Moderate (51-100): Acceptable air quality\n" +
                        "• Unhealthy for Sensitive Groups (101-150): May cause health effects\n" +
                        "• Unhealthy (151-200): Everyone may experience health effects\n" +
                        "• Very Unhealthy (201-300): Health warnings\n" +
                        "• Hazardous (301-500): Emergency conditions")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showUVIndexInfo() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("UV Index Information")
                .setMessage("UV Index levels:\n\n" +
                        "• Low (0-2): Minimal protection required\n" +
                        "• Moderate (3-5): Standard protection recommended\n" +
                        "• High (6-7): Extra protection needed\n" +
                        "• Very High (8-10): Maximum protection required\n" +
                        "• Extreme (11+): Avoid sun exposure")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAboutDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("About WeatherApp")
                .setMessage("Version 1.0.0\n\n" +
                        "Developed by:\n" +
                        "• Arsalan Khan\n" +
                        "• Zain Ansari\n" +
                        "• Kashif Ansari\n" +
                        "• Ali Abbas\n\n" +
                        "Powered by OpenWeatherMap API\n" +
                        "AQI Data from OpenAQ.org\n" +
                        "Icons by Material Design\n\n" +
                        "© 2025 All rights reserved")
                .setPositiveButton("OK", null)
                .show();
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "WeatherApp");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out this amazing weather app! It provides real-time weather updates, forecasts, air quality info, and more. Download now!");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot share app", Toast.LENGTH_SHORT).show();
        }
    }

    private void rateApp() {
        try {
            Toast.makeText(this, "Thank you for your interest in rating our app!",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open Play Store", Toast.LENGTH_SHORT).show();
        }
    }
}