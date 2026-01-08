package com.arsalankhan.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder> {

    private List<WeatherModels.ForecastItem> hourlyItems;

    public HourlyForecastAdapter(List<WeatherModels.ForecastItem> hourlyItems) {
        this.hourlyItems = hourlyItems;
    }

    public void updateData(List<WeatherModels.ForecastItem> newItems) {
        this.hourlyItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherModels.ForecastItem item = hourlyItems.get(position);

        // Time
        holder.timeText.setText(WeatherUtils.formatHour(item.timestamp));

        // Temperature
        String unit = WeatherUtils.getSavedUnit(holder.itemView.getContext());
        holder.tempText.setText(WeatherUtils.formatTemperature(item.main.temperature, unit));

        // Weather icon
        if (!item.weather.isEmpty()) {
            String iconCode = item.weather.get(0).icon;
            WeatherUtils.loadWeatherIcon(holder.itemView.getContext(),
                    iconCode, holder.weatherIcon);

            // Precipitation probability
            if (item.pop > 0) {
                holder.precipitationText.setText(String.format(Locale.getDefault(),
                        "%.0f%%", item.pop * 100));
                holder.precipitationText.setVisibility(View.VISIBLE);
            } else {
                holder.precipitationText.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return hourlyItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, tempText, precipitationText;
        ImageView weatherIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.timeText);
            tempText = itemView.findViewById(R.id.tempText);
            precipitationText = itemView.findViewById(R.id.precipitationText);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}