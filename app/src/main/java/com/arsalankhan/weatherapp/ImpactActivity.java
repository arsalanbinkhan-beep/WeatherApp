package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImpactActivity extends BaseActivity {

    private RecyclerView forecastRecyclerView;
    private DailyForecastAdapter forecastAdapter;
    private CircularProgressIndicator impactGauge;
    private TextView impactScoreText, impactStatusText, impactDescriptionText;
    private ProgressBar loadingProgress;

    private List<WeatherModels.ForecastItem> allForecastItems = new ArrayList<>();
    private Map<String, List<WeatherModels.ForecastItem>> dailyForecastMap = new HashMap<>();

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

        initViews();
        setupRecyclerView();
        loadForecastData();
    }

    private void initViews() {
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView);
        impactGauge = findViewById(R.id.impactGauge);
        impactScoreText = findViewById(R.id.impactScore);
        impactStatusText = findViewById(R.id.impactStatus);
        impactDescriptionText = findViewById(R.id.impactDescription);
        loadingProgress = findViewById(R.id.loadingProgress);
    }

    private void setupRecyclerView() {
        forecastAdapter = new DailyForecastAdapter(new ArrayList<>());
        forecastRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        forecastRecyclerView.setAdapter(forecastAdapter);
    }

    private void loadForecastData() {
        showLoading(true);

        String city = WeatherUtils.getSavedCity(this);
        String unit = WeatherUtils.getSavedUnit(this);

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<WeatherModels.ForecastResponse> call = service.getForecast(
                city,
                unit,
                // 5 days * 8 forecasts per day = 40
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.ForecastResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.ForecastResponse> call,
                                   Response<WeatherModels.ForecastResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    allForecastItems = response.body().list;
                    processForecastData();
                    calculateImpactScore();
                } else {
                    showSampleData();
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.ForecastResponse> call, Throwable t) {
                showLoading(false);
                showSampleData();
            }
        });
    }

    private void processForecastData() {
        dailyForecastMap.clear();

        // Group forecasts by day
        for (WeatherModels.ForecastItem item : allForecastItems) {
            String day = WeatherUtils.formatDay(item.timestamp);

            if (!dailyForecastMap.containsKey(day)) {
                dailyForecastMap.put(day, new ArrayList<>());
            }
            dailyForecastMap.get(day).add(item);
        }

        // Create daily forecasts
        List<DailyForecast> dailyForecasts = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (Map.Entry<String, List<WeatherModels.ForecastItem>> entry : dailyForecastMap.entrySet()) {
            String day = entry.getKey();
            List<WeatherModels.ForecastItem> dayItems = entry.getValue();

            if (dayItems.isEmpty()) continue;

            // Calculate min/max temp for the day
            double minTemp = Double.MAX_VALUE;
            double maxTemp = Double.MIN_VALUE;
            String mostCommonWeather = "Clear";
            Map<String, Integer> weatherCount = new HashMap<>();

            for (WeatherModels.ForecastItem item : dayItems) {
                minTemp = Math.min(minTemp, item.main.tempMin);
                maxTemp = Math.max(maxTemp, item.main.tempMax);

                if (!item.weather.isEmpty()) {
                    String weather = item.weather.get(0).main;
                    weatherCount.put(weather, weatherCount.getOrDefault(weather, 0) + 1);
                }
            }

            // Find most common weather
            int maxCount = 0;
            for (Map.Entry<String, Integer> weatherEntry : weatherCount.entrySet()) {
                if (weatherEntry.getValue() > maxCount) {
                    maxCount = weatherEntry.getValue();
                    mostCommonWeather = weatherEntry.getKey();
                }
            }

            // Get first item for date
            WeatherModels.ForecastItem firstItem = dayItems.get(0);
            String unit = WeatherUtils.getSavedUnit(this);

            DailyForecast forecast = new DailyForecast(
                    day,
                    WeatherUtils.formatDate(firstItem.timestamp),
                    WeatherUtils.formatTemperature(minTemp, unit),
                    WeatherUtils.formatTemperature(maxTemp, unit),
                    mostCommonWeather,
                    WeatherUtils.getWeatherIconResource(mostCommonWeather),
                    firstItem.weather.get(0).description
            );

            dailyForecasts.add(forecast);

            // Limit to 7 days
            if (dailyForecasts.size() >= 7) break;
        }

        forecastAdapter.updateData(dailyForecasts);
    }

    private void calculateImpactScore() {
        if (allForecastItems.isEmpty()) {
            setImpactScore(60, "MODERATE IMPACT",
                    "Moderate air quality and precipitation.");
            return;
        }

        int score = 75; // Base score
        int rainCount = 0;
        int stormCount = 0;
        int clearCount = 0;

        // Analyze next 24 hours (8 forecasts)
        int hoursToAnalyze = Math.min(8, allForecastItems.size());
        for (int i = 0; i < hoursToAnalyze; i++) {
            WeatherModels.ForecastItem item = allForecastItems.get(i);

            if (!item.weather.isEmpty()) {
                String weather = item.weather.get(0).main.toLowerCase();

                if (weather.contains("rain")) {
                    rainCount++;
                    score -= 10;
                } else if (weather.contains("storm")) {
                    stormCount++;
                    score -= 20;
                } else if (weather.contains("clear")) {
                    clearCount++;
                    score += 5;
                } else if (weather.contains("snow")) {
                    score -= 15;
                }
            }

            // Temperature impact
            if (item.main.temperature > 35) { // Too hot
                score -= 5;
            } else if (item.main.temperature < 5) { // Too cold
                score -= 5;
            }

            // Wind impact
            if (item.wind.speed > 10) { // Strong wind
                score -= 5;
            }
        }

        // Keep score between 0-100
        score = Math.max(0, Math.min(100, score));

        // Determine impact level
        String impactLevel;
        String description;

        if (score >= 80) {
            impactLevel = "LOW IMPACT";
            description = "Ideal weather conditions. Perfect for outdoor activities.";
        } else if (score >= 60) {
            impactLevel = "MODERATE IMPACT";
            description = "Generally good conditions. Some minor weather factors to consider.";
        } else if (score >= 40) {
            impactLevel = "HIGH IMPACT";
            description = "Significant weather impacts expected. Plan indoor activities.";
        } else {
            impactLevel = "SEVERE IMPACT";
            description = "Severe weather conditions. Avoid outdoor activities if possible.";
        }

        // Add specific warnings
        if (rainCount > 0) {
            description += "\n• " + rainCount + " hours of rain expected";
        }
        if (stormCount > 0) {
            description += "\n• Thunderstorms possible";
        }
        if (clearCount >= 6) {
            description += "\n• Mostly clear skies";
        }

        setImpactScore(score, impactLevel, description);
    }

    private void setImpactScore(int score, String level, String description) {
        impactGauge.setProgress(score);
        impactScoreText.setText(score + "%");
        impactStatusText.setText(level);
        impactDescriptionText.setText(description);

        // Set colors based on score
        int colorRes;
        if (score >= 80) {
            colorRes = android.R.color.holo_green_dark;
        } else if (score >= 60) {
            colorRes = android.R.color.holo_orange_dark;
        } else if (score >= 40) {
            colorRes = android.R.color.holo_orange_light;
        } else {
            colorRes = android.R.color.holo_red_dark;
        }

        impactStatusText.setTextColor(getResources().getColor(colorRes));
        impactGauge.setIndicatorColor(getResources().getColor(colorRes));
    }

    private void showLoading(boolean show) {
        loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        forecastRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showSampleData() {
        // Sample daily forecasts
        List<DailyForecast> sampleForecasts = new ArrayList<>();
        sampleForecasts.add(new DailyForecast("Today", "Mon, 10 Jan", "12°C", "18°C",
                "Clouds", R.drawable.ic_weather_cloud, "Cloudy"));
        sampleForecasts.add(new DailyForecast("Tue", "Tue, 11 Jan", "11°C", "17°C",
                "Rain", R.drawable.ic_weather_rain, "Light rain"));
        sampleForecasts.add(new DailyForecast("Wed", "Wed, 12 Jan", "10°C", "16°C",
                "Clear", R.drawable.ic_weather_sun, "Sunny"));

        forecastAdapter.updateData(sampleForecasts);
        setImpactScore(60, "MODERATE IMPACT",
                "Moderate air quality and precipitation.\n• 2 hours of rain expected");
    }

    // Daily Forecast model class
    static class DailyForecast {
        String day;
        String date;
        String minTemp;
        String maxTemp;
        String weather;
        int weatherIcon;
        String description;

        DailyForecast(String day, String date, String minTemp, String maxTemp,
                      String weather, int weatherIcon, String description) {
            this.day = day;
            this.date = date;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.weather = weather;
            this.weatherIcon = weatherIcon;
            this.description = description;
        }
    }
}