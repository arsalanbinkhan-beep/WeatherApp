package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingActivity extends BaseActivity {

    private SwitchMaterial switchLocation, switchNotifications;
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

        // Find views
        switchLocation = findViewById(R.id.switchLocation);
        switchNotifications = findViewById(R.id.switchNotifications);
        resetButton = findViewById(R.id.resetButton);

        // Set switch states
        switchLocation.setChecked(true);
        switchNotifications.setChecked(true);

        // Temperature unit selection
        SwitchMaterial switchCelsius = findViewById(R.id.switchCelsius);
        SwitchMaterial switchFahrenheit = findViewById(R.id.switchFahrenheit);

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

        // Reset button
        resetButton.setOnClickListener(v -> {
            // Reset to defaults
            WeatherUtils.saveCity(this, "Mumbai");
            WeatherUtils.saveUnit(this, "metric");

            if (switchLocation != null) switchLocation.setChecked(true);
            if (switchNotifications != null) switchNotifications.setChecked(true);

            Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
        });
    }
}