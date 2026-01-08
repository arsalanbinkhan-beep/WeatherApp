package com.arsalankhan.weatherapp;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface WeatherApiService {

    String BASE_URL = "https://api.openweathermap.org/";
    String API_KEY = "847c636ba223e8c59b900798dbc0dd4a"; // Get from https://openweathermap.org/api

    @GET("data/2.5/weather")
    Call<WeatherModels.WeatherResponse> getCurrentWeather(
            @Query("q") String city,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("data/2.5/weather")
    Call<WeatherModels.WeatherResponse> getCurrentWeatherByCoords(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    @GET("data/2.5/forecast")
    Call<WeatherModels.ForecastResponse> getForecast(
            @Query("q") String city,
            @Query("units") String units,
            @Query("appid") String apiKey
    );

    // Fix: This endpoint might not exist or have different parameters
    // For now, comment it out or use correct endpoint
    @GET("geo/1.0/direct")
    Call<List<WeatherModels.CitySearchResponse>> searchCities(
        @Query("q") String query,
        @Query("limit") int limit,
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