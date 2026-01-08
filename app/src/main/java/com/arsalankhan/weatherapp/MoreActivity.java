package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

        setupMenuItems();
    }

    private void setupMenuItems() {
        // Settings - Navigate to SettingActivity
        LinearLayout settingsItem = findViewById(R.id.settingsItem);
        if (settingsItem != null) {
            settingsItem.setOnClickListener(v -> navigateTo(SettingActivity.class));
        }

        // Favorites - Navigate to SearchActivity which shows favorites
        LinearLayout favoritesItem = findViewById(R.id.favoritesItem);
        if (favoritesItem != null) {
            favoritesItem.setOnClickListener(v -> {
                // Navigate to SearchActivity (which shows favorites when search is empty)
                navigateTo(SearchActivity.class);
            });
        }

        // Weather Map - Open browser
        LinearLayout weatherMapItem = findViewById(R.id.weatherMapItem);
        if (weatherMapItem != null) {
            weatherMapItem.setOnClickListener(v -> openWeatherMap());
        }

        // Air Quality - Show info dialog
        LinearLayout airQualityItem = findViewById(R.id.airQualityItem);
        if (airQualityItem != null) {
            airQualityItem.setOnClickListener(v -> showAirQualityInfo());
        }

        // UV Index - Show info dialog
        LinearLayout uvIndexItem = findViewById(R.id.uvIndexItem);
        if (uvIndexItem != null) {
            uvIndexItem.setOnClickListener(v -> showUVIndexInfo());
        }

        // About - Show about dialog
        LinearLayout aboutItem = findViewById(R.id.aboutItem);
        if (aboutItem != null) {
            aboutItem.setOnClickListener(v -> showAboutDialog());
        }

        // Share App - Share via intent
        LinearLayout shareItem = findViewById(R.id.shareItem);
        if (shareItem != null) {
            shareItem.setOnClickListener(v -> shareApp());
        }

        // Rate App - Open Play Store
        LinearLayout rateItem = findViewById(R.id.rateItem);
        if (rateItem != null) {
            rateItem.setOnClickListener(v -> rateApp());
        }
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
            // For now, just show a toast since app might not be on Play Store
            Toast.makeText(this, "Thank you for your interest in rating our app!",
                    Toast.LENGTH_SHORT).show();

            // Uncomment when app is on Play Store

            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));

        } catch (Exception e) {
            Toast.makeText(this, "Cannot open Play Store", Toast.LENGTH_SHORT).show();
        }
    }
}