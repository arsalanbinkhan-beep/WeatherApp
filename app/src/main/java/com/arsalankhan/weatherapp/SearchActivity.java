package com.arsalankhan.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends BaseActivity {

    private EditText searchEditText;
    private RecyclerView cityRecyclerView;
    private ImageButton clearSearchBtn;
    private ProgressBar searchProgress;
    private TextView emptyText;

    private CityAdapter cityAdapter;
    private List<WeatherModels.CityItem> cityList;
    private List<WeatherModels.CityItem> filteredList;

    private WeatherDatabase database;
    private WeatherApiService weatherService;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected int getBottomNavMenuId() {
        return R.id.nav_search;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        setupRecyclerView();
        setupSearch();
        loadFavoriteCities();

        weatherService = WeatherApiService.Factory.getInstance();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.searchEditText);
        cityRecyclerView = findViewById(R.id.cityRecyclerView);
        clearSearchBtn = findViewById(R.id.clearSearchBtn);
        searchProgress = findViewById(R.id.searchProgress);
        emptyText = findViewById(R.id.emptyText);

        database = WeatherDatabase.getDatabase(this);
    }

    private void setupRecyclerView() {
        cityList = new ArrayList<>();
        filteredList = new ArrayList<>();

        cityAdapter = new CityAdapter(filteredList, new CityAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(WeatherModels.CityItem city) {
                selectCity(city);
            }

            @Override
            public void onFavoriteClick(WeatherModels.CityItem city, boolean isFavorite) {
                toggleFavorite(city, isFavorite);
            }
        });

        cityRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityRecyclerView.setAdapter(cityAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    clearSearchBtn.setVisibility(View.GONE);
                    loadFavoriteCities();
                } else if (query.length() >= 2) {
                    clearSearchBtn.setVisibility(View.VISIBLE);
                    searchCities(query);
                } else {
                    clearSearchBtn.setVisibility(View.GONE);
                    filteredList.clear();
                    cityAdapter.notifyDataSetChanged();
                    showEmptyView(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearSearchBtn.setOnClickListener(v -> {
            searchEditText.setText("");
        });
    }

    private void searchCities(String query) {
        showLoading(true);

        Call<List<WeatherModels.CitySearchResponse>> call = weatherService.searchCities(
                query,
                20,
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<List<WeatherModels.CitySearchResponse>>() {
            @Override
            public void onResponse(Call<List<WeatherModels.CitySearchResponse>> call,
                                   Response<List<WeatherModels.CitySearchResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    updateSearchResults(response.body());
                } else {
                    emptyText.setText("No cities found. Try a different search.");
                    showEmptyView(true);
                }
            }

            @Override
            public void onFailure(Call<List<WeatherModels.CitySearchResponse>> call, Throwable t) {
                showLoading(false);
                emptyText.setText("Search failed. Check your connection.");
                showEmptyView(true);
            }
        });
    }

    private void updateSearchResults(List<WeatherModels.CitySearchResponse> searchResults) {
        filteredList.clear();

        for (WeatherModels.CitySearchResponse result : searchResults) {
            WeatherModels.CityItem city = new WeatherModels.CityItem(
                    result.name,
                    result.country,
                    result.state != null ? result.state : "",
                    result.lat,
                    result.lon
            );
            filteredList.add(city);
        }

        // Check which cities are favorites
        checkFavoritesForSearchResults();

        // Load temperatures for each city
        loadTemperaturesForCities();

        cityAdapter.notifyDataSetChanged();
        showEmptyView(filteredList.isEmpty());
    }

    private void loadTemperaturesForCities() {
        String unit = WeatherUtils.getSavedUnit(this);

        for (WeatherModels.CityItem city : filteredList) {
            // Fetch temperature for each city
            Call<WeatherModels.WeatherResponse> call = weatherService.getCurrentWeatherByCoords(
                    city.lat,
                    city.lon,
                    unit,
                    WeatherApiService.API_KEY
            );

            call.enqueue(new Callback<WeatherModels.WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherModels.WeatherResponse> call,
                                       Response<WeatherModels.WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        double temp = response.body().main.temperature;
                        String weatherIcon = "";
                        String weatherDesc = "";

                        if (response.body().weather != null && !response.body().weather.isEmpty()) {
                            weatherIcon = response.body().weather.get(0).icon;
                            weatherDesc = response.body().weather.get(0).description;
                        }

                        // Update the city with temperature data
                        for (WeatherModels.CityItem item : filteredList) {
                            if (item.name.equals(city.name) && item.country.equals(city.country)) {
                                item.temperature = temp;
                                item.weatherIcon = weatherIcon;
                                item.weatherDescription = weatherDesc;
                                break;
                            }
                        }

                        runOnUiThread(() -> cityAdapter.notifyDataSetChanged());
                    }
                }

                @Override
                public void onFailure(Call<WeatherModels.WeatherResponse> call, Throwable t) {
                    Log.e("SearchActivity", "Failed to load temp for " + city.name);
                }
            });
        }
    }

    private void checkFavoritesForSearchResults() {
        new Thread(() -> {
            List<FavoriteCity> favorites = database.favoriteCityDao().getAll();

            for (WeatherModels.CityItem city : filteredList) {
                city.isFavorite = false;
                for (FavoriteCity favorite : favorites) {
                    if (city.name.equals(favorite.name) &&
                            city.country.equals(favorite.country)) {
                        city.isFavorite = true;
                        break;
                    }
                }
            }

            runOnUiThread(() -> cityAdapter.notifyDataSetChanged());
        }).start();
    }

    private void loadFavoriteCities() {
        new Thread(() -> {
            List<FavoriteCity> favorites = database.favoriteCityDao().getAll();

            filteredList.clear();
            for (FavoriteCity favorite : favorites) {
                WeatherModels.CityItem city = new WeatherModels.CityItem(
                        favorite.name,
                        favorite.country,
                        favorite.state,
                        favorite.lat,
                        favorite.lon
                );
                city.isFavorite = true;

                // Load temperature for favorite city
                loadTemperatureForCity(city);

                filteredList.add(city);
            }

            runOnUiThread(() -> {
                cityAdapter.notifyDataSetChanged();
                showEmptyView(filteredList.isEmpty());
                if (filteredList.isEmpty()) {
                    emptyText.setText("No favorite cities. Search and add some!");
                }
            });
        }).start();
    }

    private void loadTemperatureForCity(WeatherModels.CityItem city) {
        String unit = WeatherUtils.getSavedUnit(this);

        Call<WeatherModels.WeatherResponse> call = weatherService.getCurrentWeatherByCoords(
                city.lat,
                city.lon,
                unit,
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<WeatherModels.WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherModels.WeatherResponse> call,
                                   Response<WeatherModels.WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    city.temperature = response.body().main.temperature;
                    if (response.body().weather != null && !response.body().weather.isEmpty()) {
                        city.weatherIcon = response.body().weather.get(0).icon;
                        city.weatherDescription = response.body().weather.get(0).description;
                    }
                    runOnUiThread(() -> cityAdapter.notifyDataSetChanged());
                }
            }

            @Override
            public void onFailure(Call<WeatherModels.WeatherResponse> call, Throwable t) {
                Log.e("SearchActivity", "Failed to load temp for favorite " + city.name);
            }
        });
    }

    private void selectCity(WeatherModels.CityItem city) {
        // Save selected city
        WeatherUtils.saveLocation(this, city.name, city.country, city.lat, city.lon);

        // Save preference to NOT use location (use saved city instead)
        getSharedPreferences("WeatherPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("use_location", false)
                .apply();

        // Create result intent with city data
        Intent resultIntent = new Intent();
        resultIntent.putExtra("city_name", city.name);
        resultIntent.putExtra("country", city.country);
        resultIntent.putExtra("latitude", city.lat);
        resultIntent.putExtra("longitude", city.lon);

        // Set result and finish
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Selected: " + city.name + ", " + city.country,
                Toast.LENGTH_SHORT).show();

        // Close SearchActivity and return to MainActivity
        finish();
    }

    private void toggleFavorite(WeatherModels.CityItem city, boolean isFavorite) {
        new Thread(() -> {
            if (isFavorite) {
                // Remove from favorites
                FavoriteCity favorite = database.favoriteCityDao()
                        .findByNameAndCountry(city.name, city.country);
                if (favorite != null) {
                    database.favoriteCityDao().delete(favorite);
                    runOnUiThread(() -> {
                        Toast.makeText(SearchActivity.this, "Removed from favorites",
                                Toast.LENGTH_SHORT).show();

                        // If we're showing favorites list, update UI
                        if (searchEditText.getText().toString().isEmpty()) {
                            loadFavoriteCities();
                        }
                    });
                }
            } else {
                // Add to favorites
                FavoriteCity favorite = new FavoriteCity(
                        city.name, city.country, city.state, city.lat, city.lon);
                database.favoriteCityDao().insert(favorite);
                runOnUiThread(() -> {
                    Toast.makeText(SearchActivity.this, "Added to favorites",
                            Toast.LENGTH_SHORT).show();
                    city.isFavorite = true;
                    cityAdapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void showLoading(boolean show) {
        searchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        cityRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyText.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        cityRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}