package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

        WeatherApiService service = WeatherApiService.Factory.getInstance();
        Call<List<WeatherModels.CitySearchResponse>> call = service.searchCities(
                query,
                20, // Limit to 20 results
                WeatherApiService.API_KEY
        );

        call.enqueue(new Callback<List<WeatherModels.CitySearchResponse>>() {
            @Override
            public void onResponse(Call<List<WeatherModels.CitySearchResponse>> call,
                                   Response<List<WeatherModels.CitySearchResponse>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    updateSearchResults(response.body());
                } else {
                    Toast.makeText(SearchActivity.this,
                            "Failed to search cities", Toast.LENGTH_SHORT).show();
                    showEmptyView(true);
                }
            }

            @Override
            public void onFailure(Call<List<WeatherModels.CitySearchResponse>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(SearchActivity.this,
                        "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    result.state,
                    result.lat,
                    result.lon
            );
            filteredList.add(city);
        }

        // Check which cities are favorites
        checkFavoritesForSearchResults();

        cityAdapter.notifyDataSetChanged();
        showEmptyView(filteredList.isEmpty());
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
                filteredList.add(city);
            }

            runOnUiThread(() -> {
                cityAdapter.notifyDataSetChanged();
                showEmptyView(filteredList.isEmpty());
            });
        }).start();
    }

    private void selectCity(WeatherModels.CityItem city) {
        // Save selected city
        WeatherUtils.saveLocation(this, city.name, city.country, city.lat, city.lon);

        Toast.makeText(this, "Selected: " + city.name + ", " + city.country,
                Toast.LENGTH_SHORT).show();

        // Navigate back to main activity
        navigateTo(MainActivity.class);
    }

    private void toggleFavorite(WeatherModels.CityItem city, boolean isFavorite) {
        new Thread(() -> {
            if (isFavorite) {
                // Add to favorites
                FavoriteCity favorite = new FavoriteCity(
                        city.name, city.country, city.state, city.lat, city.lon);
                database.favoriteCityDao().insert(favorite);
                runOnUiThread(() ->
                        Toast.makeText(SearchActivity.this, "Added to favorites",
                                Toast.LENGTH_SHORT).show());
            } else {
                // Remove from favorites
                FavoriteCity favorite = database.favoriteCityDao()
                        .findByNameAndCountry(city.name, city.country);
                if (favorite != null) {
                    database.favoriteCityDao().delete(favorite);
                    runOnUiThread(() ->
                            Toast.makeText(SearchActivity.this, "Removed from favorites",
                                    Toast.LENGTH_SHORT).show());

                    // If we're showing favorites list, remove from UI
                    if (searchEditText.getText().toString().isEmpty()) {
                        loadFavoriteCities();
                    }
                }
            }
        }).start();
    }

    private void showLoading(boolean show) {
        searchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        cityRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        cityRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}