package com.arsalankhan.weatherapp;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    String API_KEY = "847c636ba223e8c59b900798dbc0dd4a"; // Get from https://openweathermap.org/api

    @GET("weather")
    Call<WeatherModels.WeatherResponse> getCurrentWeather(
            @Query("q") String city,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("weather")
    Call<WeatherModels.WeatherResponse> getCurrentWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("forecast")
    Call<WeatherModels.ForecastResponse> getForecast(
            @Query("q") String city,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    class Factory {
        private static WeatherApiService service;

        public static WeatherApiService getInstance() {
            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                service = retrofit.create(WeatherApiService.class);
            }
            return service;
        }
    }
}