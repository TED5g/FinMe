package com.example.finme.network;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.example.finme.model.Location;


public interface GeocodingApiService {
    @GET("geo/1.0/direct")
    Call<List<Location>> getCoordinates(@Query("q") String cityName, @Query("appid") String apiKey);
}
