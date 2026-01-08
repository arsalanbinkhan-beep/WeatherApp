package com.arsalankhan.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherUtils {

    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_CITY = "selected_city";
    private static final String KEY_COUNTRY = "selected_country";
    private static final String KEY_LAT = "selected_lat";
    private static final String KEY_LON = "selected_lon";
    private static final String KEY_UNIT = "temperature_unit";
    private static final String DEFAULT_CITY = "Mumbai";
    private static final String DEFAULT_COUNTRY = "IN";
    private static final String DEFAULT_UNIT = "metric";

    // Save location
    public static void saveLocation(Context context, String city, String country, double lat, double lon) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_CITY, city)
                .putString(KEY_COUNTRY, country)
                .putString(KEY_LAT, String.valueOf(lat))
                .putString(KEY_LON, String.valueOf(lon))
                .apply();
    }

    public static String getSavedCity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CITY, DEFAULT_CITY);
    }

    public static String getSavedCountry(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_COUNTRY, DEFAULT_COUNTRY);
    }

    public static double getSavedLat(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Double.parseDouble(prefs.getString(KEY_LAT, "19.0760"));
    }

    public static double getSavedLon(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return Double.parseDouble(prefs.getString(KEY_LON, "72.8777"));
    }

    public static void saveUnit(Context context, String unit) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_UNIT, unit).apply();
    }

    public static String getSavedUnit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_UNIT, DEFAULT_UNIT);
    }

    public static String formatTemperature(double temp, String unit) {
        if ("imperial".equals(unit)) {
            return String.format(Locale.getDefault(), "%.0f°F", temp);
        }
        return String.format(Locale.getDefault(), "%.0f°C", temp);
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatDay(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String formatHour(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh a", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    public static String getWeatherIconUrl(String iconCode) {
        return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }

    public static void loadWeatherIcon(Context context, String iconCode, ImageView imageView) {
        String iconUrl = getWeatherIconUrl(iconCode);
        Glide.with(context)
                .load(iconUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(getWeatherIconResource("default"))
                .error(getWeatherIconResource("default"))
                .into(imageView);
    }

    public static int getWeatherIconResource(String weatherMain) {
        if (weatherMain == null) return R.drawable.ic_weather_cloud_sun;

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

    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return "";

        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    public static String getWindDirection(int degrees) {
        if (degrees >= 337.5 || degrees < 22.5) return "N";
        if (degrees >= 22.5 && degrees < 67.5) return "NE";
        if (degrees >= 67.5 && degrees < 112.5) return "E";
        if (degrees >= 112.5 && degrees < 157.5) return "SE";
        if (degrees >= 157.5 && degrees < 202.5) return "S";
        if (degrees >= 202.5 && degrees < 247.5) return "SW";
        if (degrees >= 247.5 && degrees < 292.5) return "W";
        return "NW";
    }

    public static String getAQILevel(int aqi) {
        switch (aqi) {
            case 1: return "Good";
            case 2: return "Fair";
            case 3: return "Moderate";
            case 4: return "Poor";
            case 5: return "Very Poor";
            default: return "Unknown";
        }
    }

    public static int getAQIColor(Context context, int aqi) {
        switch (aqi) {
            case 1: return context.getResources().getColor(R.color.aqi_good);
            case 2: return context.getResources().getColor(R.color.aqi_fair);
            case 3: return context.getResources().getColor(R.color.aqi_moderate);
            case 4: return context.getResources().getColor(R.color.aqi_poor);
            case 5: return context.getResources().getColor(R.color.aqi_very_poor);
            default: return context.getResources().getColor(R.color.grey);
        }
    }

    public static String getUVILevel(double uvi) {
        if (uvi <= 2) return "Low";
        if (uvi <= 5) return "Moderate";
        if (uvi <= 7) return "High";
        if (uvi <= 10) return "Very High";
        return "Extreme";
    }
}