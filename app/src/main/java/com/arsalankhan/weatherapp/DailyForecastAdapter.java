package com.arsalankhan.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {

    private List<ImpactActivity.DailyForecast> forecasts;

    public DailyForecastAdapter(List<ImpactActivity.DailyForecast> forecasts) {
        this.forecasts = forecasts;
    }

    public void updateData(List<ImpactActivity.DailyForecast> newForecasts) {
        this.forecasts = newForecasts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_forecast, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImpactActivity.DailyForecast forecast = forecasts.get(position);

        holder.dayText.setText(forecast.day);
        holder.dateText.setText(forecast.date);
        holder.minTempText.setText(forecast.minTemp);
        holder.maxTempText.setText(forecast.maxTemp);
        holder.weatherIcon.setImageResource(forecast.weatherIcon);
        holder.descriptionText.setText(forecast.description);
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayText, dateText, minTempText, maxTempText, descriptionText;
        ImageView weatherIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
            dateText = itemView.findViewById(R.id.dateText);
            minTempText = itemView.findViewById(R.id.minTempText);
            maxTempText = itemView.findViewById(R.id.maxTempText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            weatherIcon = itemView.findViewById(R.id.weatherIcon);
        }
    }
}