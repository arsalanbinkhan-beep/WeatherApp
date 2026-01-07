package com.arsalankhan.weatherapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    private EditText searchEditText;
    private RecyclerView cityRecyclerView;
    private CityAdapter cityAdapter;
    private List<WeatherModels.CityItem> cityList;
    private List<WeatherModels.CityItem> filteredList;

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

        searchEditText = findViewById(R.id.searchEditText);
        cityRecyclerView = findViewById(R.id.cityRecyclerView);

        // Setup RecyclerView
        cityRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize city list (you can load from API or local database)
        initCityList();

        cityAdapter = new CityAdapter(filteredList, city -> {
            // Save selected city
            WeatherUtils.saveCity(this, city.name);
            Toast.makeText(this, "Selected: " + city.name, Toast.LENGTH_SHORT).show();

            // Go back to main activity
            navigateTo(MainActivity.class);
        });

        cityRecyclerView.setAdapter(cityAdapter);

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCities(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initCityList() {
        cityList = new ArrayList<>();
        // Add some major cities
        cityList.add(new WeatherModels.CityItem("Mumbai", "IN", 19.0760, 72.8777));
        cityList.add(new WeatherModels.CityItem("Delhi", "IN", 28.7041, 77.1025));
        cityList.add(new WeatherModels.CityItem("Bangalore", "IN", 12.9716, 77.5946));
        cityList.add(new WeatherModels.CityItem("London", "UK", 51.5074, -0.1278));
        cityList.add(new WeatherModels.CityItem("New York", "US", 40.7128, -74.0060));
        cityList.add(new WeatherModels.CityItem("Tokyo", "JP", 35.6762, 139.6503));
        cityList.add(new WeatherModels.CityItem("Paris", "FR", 48.8566, 2.3522));
        cityList.add(new WeatherModels.CityItem("Dubai", "AE", 25.2048, 55.2708));
        cityList.add(new WeatherModels.CityItem("Singapore", "SG", 1.3521, 103.8198));
        cityList.add(new WeatherModels.CityItem("Sydney", "AU", -33.8688, 151.2093));

        filteredList = new ArrayList<>(cityList);
    }

    private void filterCities(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(cityList);
        } else {
            for (WeatherModels.CityItem city : cityList) {
                if (city.name.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(city);
                }
            }
        }
        cityAdapter.notifyDataSetChanged();
    }
}