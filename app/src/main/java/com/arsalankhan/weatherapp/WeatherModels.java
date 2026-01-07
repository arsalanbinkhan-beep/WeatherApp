package com.arsalankhan.weatherapp;

import com.google.gson.annotations.SerializedName;

public class WeatherModels {

    // Main Weather Response
    public static class WeatherResponse {
        @SerializedName("name")
        public String cityName;

        @SerializedName("main")
        public MainData main;

        @SerializedName("weather")
        public Weather[] weather;

        @SerializedName("wind")
        public Wind wind;

        @SerializedName("sys")
        public Sys sys;

        @SerializedName("dt")
        public long timestamp;
    }

    public static class MainData {
        @SerializedName("temp")
        public double temperature;

        @SerializedName("feels_like")
        public double feelsLike;

        @SerializedName("temp_min")
        public double tempMin;

        @SerializedName("temp_max")
        public double tempMax;

        @SerializedName("pressure")
        public int pressure;

        @SerializedName("humidity")
        public int humidity;
    }

    public static class Weather {
        @SerializedName("main")
        public String main;

        @SerializedName("description")
        public String description;

        @SerializedName("icon")
        public String icon;
    }

    public static class Wind {
        @SerializedName("speed")
        public double speed;

        @SerializedName("deg")
        public int degree;
    }

    public static class Sys {
        @SerializedName("country")
        public String country;

        @SerializedName("sunrise")
        public long sunrise;

        @SerializedName("sunset")
        public long sunset;
    }

    // For 5-day forecast
    public static class ForecastResponse {
        @SerializedName("list")
        public ForecastItem[] list;

        @SerializedName("city")
        public City city;
    }

    public static class ForecastItem {
        @SerializedName("dt")
        public long timestamp;

        @SerializedName("main")
        public MainData main;

        @SerializedName("weather")
        public Weather[] weather;

        @SerializedName("dt_txt")
        public String dateText;
    }

    public static class City {
        @SerializedName("name")
        public String name;

        @SerializedName("country")
        public String country;
    }

    // For Search/Location
    public static class CityItem {
        public String name;
        public String country;
        public double lat;
        public double lon;
        public double temperature;
        public String weatherIcon;

        public CityItem(String name, String country, double lat, double lon) {
            this.name = name;
            this.country = country;
            this.lat = lat;
            this.lon = lon;
        }
    }
}