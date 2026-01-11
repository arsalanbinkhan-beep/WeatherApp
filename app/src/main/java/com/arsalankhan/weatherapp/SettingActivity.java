package com.arsalankhan.weatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingActivity extends BaseActivity {  // Changed from AppCompatActivity to BaseActivity

    private SwitchMaterial switchLocation, switchNotifications, switchGPSCaching, switchAutoRefresh;
    private SwitchMaterial switchCelsius, switchFahrenheit;
    private Button resetButton;

    private SharedPreferences preferences;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_more;  // This should match the menu item for Settings
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // This will call BaseActivity.onCreate()

        preferences = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);

        // Find all views
        findViews();

        // Set initial switch states
        initializeSwitches();

        // Setup temperature unit selection
        setupTemperatureUnits();

        // Setup reset button
        setupResetButton();
    }

    private void findViews() {
        switchLocation = findViewById(R.id.switchLocation);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchGPSCaching = findViewById(R.id.switchGPSCaching);
        switchAutoRefresh = findViewById(R.id.switchAutoRefresh);
        switchCelsius = findViewById(R.id.switchCelsius);
        switchFahrenheit = findViewById(R.id.switchFahrenheit);
        resetButton = findViewById(R.id.resetButton);
    }

    private void initializeSwitches() {
        // Load saved preferences
        boolean locationEnabled = preferences.getBoolean("location_enabled", true);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", true);
        boolean gpsCachingEnabled = preferences.getBoolean("gps_caching", false);
        boolean autoRefreshEnabled = preferences.getBoolean("auto_refresh", false);

        // Set switch states
        switchLocation.setChecked(locationEnabled);
        switchNotifications.setChecked(notificationsEnabled);
        switchGPSCaching.setChecked(gpsCachingEnabled);
        switchAutoRefresh.setChecked(autoRefreshEnabled);

        // Add listeners
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("location_enabled", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Location services enabled" : "Location services disabled",
                    Toast.LENGTH_SHORT).show();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        switchGPSCaching.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("gps_caching", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "GPS caching enabled" : "GPS caching disabled",
                    Toast.LENGTH_SHORT).show();
        });

        switchAutoRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("auto_refresh", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Auto-refresh enabled" : "Auto-refresh disabled",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTemperatureUnits() {
        if (switchCelsius != null && switchFahrenheit != null) {
            String currentUnit = WeatherUtils.getSavedUnit(this);

            if (currentUnit.equals("metric")) {
                switchCelsius.setChecked(true);
                switchFahrenheit.setChecked(false);
            } else {
                switchCelsius.setChecked(false);
                switchFahrenheit.setChecked(true);
            }

            switchCelsius.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    switchFahrenheit.setChecked(false);
                    WeatherUtils.saveUnit(this, "metric");
                    Toast.makeText(this, "Temperature unit set to Celsius", Toast.LENGTH_SHORT).show();
                }
            });

            switchFahrenheit.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    switchCelsius.setChecked(false);
                    WeatherUtils.saveUnit(this, "imperial");
                    Toast.makeText(this, "Temperature unit set to Fahrenheit", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupResetButton() {
        resetButton.setOnClickListener(v -> {
            // Reset to defaults
            WeatherUtils.saveLocation(this, "Mumbai", "IN", 19.0760, 72.8777);
            WeatherUtils.saveUnit(this, "metric");

            // Reset all switches
            switchLocation.setChecked(true);
            switchNotifications.setChecked(true);
            switchGPSCaching.setChecked(false);
            switchAutoRefresh.setChecked(false);
            switchCelsius.setChecked(true);
            switchFahrenheit.setChecked(false);

            // Reset preferences
            preferences.edit()
                    .putBoolean("location_enabled", true)
                    .putBoolean("notifications_enabled", true)
                    .putBoolean("gps_caching", false)
                    .putBoolean("auto_refresh", false)
                    .apply();

            Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
        });
    }
}