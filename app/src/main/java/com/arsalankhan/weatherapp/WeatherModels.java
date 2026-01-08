package com.arsalankhan.weatherapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherModels {

    // Current Weather Response
    public static class WeatherResponse {
        @SerializedName("name")
        public String cityName;

        @SerializedName("coord")
        public Coordinates coord;

        @SerializedName("main")
        public MainData main;

        @SerializedName("weather")
        public List<Weather> weather;

        @SerializedName("wind")
        public Wind wind;

        @SerializedName("sys")
        public Sys sys;

        @SerializedName("dt")
        public long timestamp;

        @SerializedName("timezone")
        public int timezone;

        @SerializedName("visibility")
        public int visibility;

        @SerializedName("clouds")
        public Clouds clouds;
    }

    public static class Coordinates {
        @SerializedName("lat")
        public double lat;

        @SerializedName("lon")
        public double lon;
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

        @SerializedName("sea_level")
        public int seaLevel;

        @SerializedName("grnd_level")
        public int groundLevel;
    }

    public static class Weather {
        @SerializedName("id")
        public int id;

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

        @SerializedName("gust")
        public double gust;
    }

    public static class Sys {
        @SerializedName("country")
        public String country;

        @SerializedName("sunrise")
        public long sunrise;

        @SerializedName("sunset")
        public long sunset;
    }

    public static class Clouds {
        @SerializedName("all")
        public int all;
    }

    // Forecast Response
    public static class ForecastResponse {
        @SerializedName("list")
        public List<ForecastItem> list;

        @SerializedName("city")
        public City city;
    }

    public static class ForecastItem {
        @SerializedName("dt")
        public long timestamp;

        @SerializedName("main")
        public MainData main;

        @SerializedName("weather")
        public List<Weather> weather;

        @SerializedName("wind")
        public Wind wind;

        @SerializedName("visibility")
        public int visibility;

        @SerializedName("pop")
        public double pop; // Probability of precipitation

        @SerializedName("dt_txt")
        public String dateText;
    }

    public static class City {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("country")
        public String country;

        @SerializedName("coord")
        public Coordinates coord;

        @SerializedName("timezone")
        public int timezone;
    }

    // City Search Response
    public static class CitySearchResponse {
        @SerializedName("name")
        public String name;

        @SerializedName("lat")
        public double lat;

        @SerializedName("lon")
        public double lon;

        @SerializedName("country")
        public String country;

        @SerializedName("state")
        public String state;
    }

    // Air Quality Response
    public static class AirQualityResponse {
        @SerializedName("list")
        public List<AirQualityData> list;
    }

    public static class AirQualityData {
        @SerializedName("main")
        public AirQualityMain main;

        @SerializedName("components")
        public AirQualityComponents components;
    }

    public static class AirQualityMain {
        @SerializedName("aqi")
        public int aqi;
    }

    public static class AirQualityComponents {
        @SerializedName("co")
        public double co;

        @SerializedName("no")
        public double no;

        @SerializedName("no2")
        public double no2;

        @SerializedName("o3")
        public double o3;

        @SerializedName("so2")
        public double so2;

        @SerializedName("pm2_5")
        public double pm25;

        @SerializedName("pm10")
        public double pm10;

        @SerializedName("nh3")
        public double nh3;
    }

    // App Models
    public static class CityItem {
        public String name;
        public String country;
        public String state;
        public double lat;
        public double lon;
        public double temperature;
        public String weatherIcon;
        public String weatherDescription;
        public boolean isFavorite;

        public CityItem(String name, String country, String state, double lat, double lon) {
            this.name = name;
            this.country = country;
            this.state = state;
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static class WeatherDetail {
        public String title;
        public String value;
        public String unit;
        public int iconRes;

        public WeatherDetail(String title, String value, String unit, int iconRes) {
            this.title = title;
            this.value = value;
            this.unit = unit;
            this.iconRes = iconRes;
        }
    }
}