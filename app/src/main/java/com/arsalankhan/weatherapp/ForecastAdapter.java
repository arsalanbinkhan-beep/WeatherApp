package com.arsalankhan.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private List<WeatherModels.ForecastItem> forecastItems;

    public ForecastAdapter(List<WeatherModels.ForecastItem> forecastItems) {
        this.forecastItems = forecastItems;
    }

    public void updateData(List<WeatherModels.ForecastItem> newItems) {
        this.forecastItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherModels.ForecastItem item = forecastItems.get(position);

        // Format time
        String time = formatTime(item.timestamp);
        holder.timeText.setText(time);

        // Temperature - FIX: Pass only unit parameter
        String unit = WeatherUtils.getSavedUnit(holder.itemView.getContext());
        holder.mainTemp.setText(WeatherUtils.formatTemperature(item.main.temperature, unit));

        // Weather description
        if (item.weather != null && !item.weather.isEmpty()) {
            holder.descText.setText(item.weather.get(0).description);

            // Load weather icon - FIX: Use correct method
            if (item.weather.get(0).icon != null) {
                WeatherUtils.loadWeatherIcon(holder.itemView.getContext(),
                        item.weather.get(0).icon, holder.forecastIcon);
            }
        }

        // High/Low temp
        holder.subTemp.setText("H: " + WeatherUtils.formatTemperature(item.main.tempMax, unit) +
                " L: " + WeatherUtils.formatTemperature(item.main.tempMin, unit));
    }

    @Override
    public int getItemCount() {
        return forecastItems.size();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh a\ndd MMM", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView forecastIcon;
        TextView timeText, descText, mainTemp, subTemp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            forecastIcon = itemView.findViewById(R.id.forecastIcon);
            timeText = itemView.findViewById(R.id.timeText);
            descText = itemView.findViewById(R.id.descText);
            mainTemp = itemView.findViewById(R.id.mainTemp);
            subTemp = itemView.findViewById(R.id.subTemp);
        }
    }
}