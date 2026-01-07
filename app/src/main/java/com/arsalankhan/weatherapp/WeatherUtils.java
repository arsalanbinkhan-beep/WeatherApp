package com.arsalankhan.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherUtils {

    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_CITY = "selected_city";
    private static final String KEY_UNIT = "temperature_unit";
    private static final String DEFAULT_CITY = "Mumbai";
    private static final String DEFAULT_UNIT = "metric"; // Celsius

    public static void saveCity(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CITY, city).apply();
    }

    public static String getSavedCity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CITY, DEFAULT_CITY);
    }

    public static void saveUnit(Context context, String unit) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_UNIT, unit).apply();
    }

    public static String getSavedUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_UNIT, DEFAULT_UNIT);
    }

    public static String formatTemperature(double temp) {
        return String.format(Locale.getDefault(), "%.0f°C", temp);
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String getWeatherIconUrl(String iconCode) {
        return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }

    public static void loadWeatherIcon(Context context, String iconCode, ImageView imageView) {
        String iconUrl = getWeatherIconUrl(iconCode);
        Glide.with(context)
                .load(iconUrl)
                .placeholder(R.drawable.ic_weather_cloud_sun)
                .error(R.drawable.ic_weather_cloud_sun)
                .into(imageView);
    }

    public static int getWeatherIconResource(String weatherMain) {
        switch (weatherMain.toLowerCase()) {
            case "clear":
                return R.drawable.ic_weather_sun;
            case "clouds":
                return R.drawable.ic_weather_cloud;
            case "rain":
                return R.drawable.ic_weather_rain;
            case "snow":
                return R.drawable.ic_weather_snow;
            case "thunderstorm":
                return R.drawable.ic_weather_thunderstorm;
            case "drizzle":
                return R.drawable.ic_weather_drizzle;
            case "mist":
            case "fog":
            case "haze":
                return R.drawable.ic_weather_fog;
            default:
                return R.drawable.ic_weather_cloud_sun;
        }
    }
}