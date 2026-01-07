package com.arsalankhan.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private List<WeatherModels.CityItem> cities;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(WeatherModels.CityItem city);
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
        holder.citySub.setText(city.country);

        // You can fetch temperature here if you want
        // For now, showing static temp
        holder.itemTemp.setText("--°C");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityName, citySub, itemTemp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.cityName);
            citySub = itemView.findViewById(R.id.citySub);
            itemTemp = itemView.findViewById(R.id.itemTemp);
        }
    }
}