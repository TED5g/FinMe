package com.example.finme.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import com.example.finme.model.WeatherResponse;


public interface WeatherApiService {
    @GET("data/2.5/weather")
    Call<WeatherResponse> getCurrentWeather(@Query("lat") double latitude, @Query("lon") double longitude, @Query("appid") String apiKey);
}