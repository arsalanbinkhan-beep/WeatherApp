package com.arsalankhan.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherDetailsAdapter extends RecyclerView.Adapter<WeatherDetailsAdapter.ViewHolder> {

    private List<WeatherModels.WeatherDetail> details;

    public WeatherDetailsAdapter(List<WeatherModels.WeatherDetail> details) {
        this.details = details;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherModels.WeatherDetail detail = details.get(position);

        holder.titleText.setText(detail.title);
        holder.valueText.setText(detail.value);
        holder.unitText.setText(detail.unit);
        holder.iconView.setImageResource(detail.iconRes);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, valueText, unitText;
        ImageView iconView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            valueText = itemView.findViewById(R.id.valueText);
            unitText = itemView.findViewById(R.id.unitText);
            iconView = itemView.findViewById(R.id.iconView);
        }
    }
}