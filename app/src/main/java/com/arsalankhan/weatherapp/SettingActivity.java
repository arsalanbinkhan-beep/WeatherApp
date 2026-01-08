package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingActivity extends BaseActivity {

    private SwitchMaterial switchLocation, switchNotifications, switchGPSCaching, switchAutoRefresh;
    private SwitchMaterial switchCelsius, switchFahrenheit;
    private Button resetButton;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_more;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        // Set default states
        if (switchLocation != null) switchLocation.setChecked(true);
        if (switchNotifications != null) switchNotifications.setChecked(true);
        if (switchGPSCaching != null) switchGPSCaching.setChecked(false);
        if (switchAutoRefresh != null) switchAutoRefresh.setChecked(false);

        // Add listeners if needed
        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this,
                    isChecked ? "Location services enabled" : "Location services disabled",
                    Toast.LENGTH_SHORT).show();
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
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
            if (switchLocation != null) switchLocation.setChecked(true);
            if (switchNotifications != null) switchNotifications.setChecked(true);
            if (switchGPSCaching != null) switchGPSCaching.setChecked(false);
            if (switchAutoRefresh != null) switchAutoRefresh.setChecked(false);
            if (switchCelsius != null) switchCelsius.setChecked(true);
            if (switchFahrenheit != null) switchFahrenheit.setChecked(false);

            Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
        });
    }
}