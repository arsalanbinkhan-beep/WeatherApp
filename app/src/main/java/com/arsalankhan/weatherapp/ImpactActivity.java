package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImpactActivity extends BaseActivity {

    private RecyclerView forecastRecyclerView;
    private ForecastAdapter forecastAdapter;
    private TextView impactScoreText, impactStatusText;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_impact;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_impact;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
        impactScoreText = findViewById(R.id.impactScore);
        impactStatusText = findViewById(R.id.impactStatus);

        // Setup RecyclerView
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forecastAdapter = new ForecastAdapter(new ArrayList<>());
        forecastRecyclerView.setAdapter(forecastAdapter);

        // Load forecast data
        loadForecastData();
    }

    private void loadForecastData() {
        String city = WeatherUtils.getSavedCity(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<WeatherModels.ForecastResponse> call = service.getForecast(
                city,
                "metric",
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.ForecastResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.ForecastResponse> call, Response<WeatherModels.ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateForecastUI(response.body());
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.ForecastResponse> call, Throwable t) {
                showSampleForecast();
            }
        });
    }

    private void updateForecastUI(WeatherModels.ForecastResponse forecast) {
        List<WeatherModels.ForecastItem> items = new ArrayList<>();

        // Take first 5 forecast items (for next 5 days/times)
        int count = Math.min(forecast.list.length, 5);
        for (int i = 0; i < count; i++) {
            items.add(forecast.list[i]);
        }

        forecastAdapter.updateData(items);

        // Calculate impact score based on weather conditions
        calculateImpactScore(items);
    }

    private void calculateImpactScore(List<WeatherModels.ForecastItem> forecastItems) {
        // Simple impact calculation
        int score = 75; // Base score

        for (WeatherModels.ForecastItem item : forecastItems) {
            if (item.weather[0].main.equalsIgnoreCase("rain")) {
                score -= 15;
            } else if (item.weather[0].main.equalsIgnoreCase("clear")) {
                score += 10;
            }
        }

        score = Math.max(0, Math.min(100, score)); // Keep between 0-100

        impactScoreText.setText(score + "%");

        if (score >= 80) {
            impactStatusText.setText("LOW IMPACT");
            impactStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (score >= 50) {
            impactStatusText.setText("MODERATE IMPACT");
            impactStatusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            impactStatusText.setText("HIGH IMPACT");
            impactStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showSampleForecast() {
        // Sample forecast data
        List<WeatherModels.ForecastItem> sampleItems = new ArrayList<>();
        forecastAdapter.updateData(sampleItems);

        impactScoreText.setText("60%");
        impactStatusText.setText("MODERATE IMPACT");
    }
}