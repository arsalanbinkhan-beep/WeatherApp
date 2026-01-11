package com.arsalankhan.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private List<WeatherModels.CityItem> cities;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(WeatherModels.CityItem city);
        void onFavoriteClick(WeatherModels.CityItem city, boolean isFavorite);
    }

    public CityAdapter(List<WeatherModels.CityItem> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherModels.CityItem city = cities.get(position);
        holder.cityName.setText(city.name);

        // Format location text
        String locationText = city.country;
        if (city.state != null && !city.state.isEmpty()) {
            locationText = city.state + ", " + locationText;
        }
        holder.citySub.setText(locationText);

        // Set temperature
        if (city.temperature != 0) {
            String unit = WeatherUtils.getSavedUnit(holder.itemView.getContext());
            holder.itemTemp.setText(WeatherUtils.formatTemperature(city.temperature, unit));

            // Load weather icon if available
            if (city.weatherIcon != null && !city.weatherIcon.isEmpty()) {
                WeatherUtils.loadWeatherIcon(holder.itemView.getContext(),
                        city.weatherIcon, holder.weatherIcon);
            }
        } else {
            holder.itemTemp.setText("--°C");
            holder.weatherIcon.setImageResource(R.drawable.ic_weather_cloud_sun);
        }

        // Set favorite icon
        if (city.isFavorite) {
            holder.favoriteBtn.setImageResource(R.drawable.ic_favorite_filled);
            holder.favoriteBtn.setContentDescription("Remove from favorites");
        } else {
            holder.favoriteBtn.setImageResource(R.drawable.ic_favorite_border);
            holder.favoriteBtn.setContentDescription("Add to favorites");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city);
            }
        });

        holder.favoriteBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(city, !city.isFavorite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityName, citySub, itemTemp;
        ImageButton favoriteBtn;
        ImageView weatherIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.cityName);
            citySub = itemView.findViewById(R.id.citySub);
            itemTemp = itemView.findViewById(R.id.itemTemp);
            favoriteBtn = itemView.findViewById(R.id.favoriteBtn);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}