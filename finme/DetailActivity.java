package com.example.finme;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finme.model.CityCodeMap;
import com.example.finme.model.Location;
import com.example.finme.model.WeatherResponse;
import com.example.finme.network.ApiClient;
import com.example.finme.network.GeocodingApiService;
import com.example.finme.network.WeatherApiService;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailActivity extends AppCompatActivity {
    private TextView weatherInfo;
    private TextView populationInfo;
    private ImageView populationImage;
    private TextView cityNameTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        weatherInfo = findViewById(R.id.weather_info);
        populationInfo = findViewById(R.id.population_info);
        populationImage = findViewById(R.id.population_image);
        cityNameTextView = findViewById(R.id.city_name_textview);

        String cityName = getIntent().getStringExtra("CITY_NAME");
        if (cityName != null && !cityName.isEmpty()) {
            cityNameTextView.setText(cityName);
        }

        fetchWeather(cityName);
        fetchPopulationInfo(cityName);
    }


    private void fetchWeather(String cityName) {
        GeocodingApiService geocodingService = ApiClient.getClient().create(GeocodingApiService.class);
        geocodingService.getCoordinates(cityName, BuildConfig.OPEN_WEATHER_API_KEY).enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Location location = response.body().get(0);
                    WeatherApiService weatherService = ApiClient.getClient().create(WeatherApiService.class);
                    weatherService.getCurrentWeather(location.lat, location.lon, BuildConfig.OPEN_WEATHER_API_KEY)
                            .enqueue(new Callback<WeatherResponse>() {
                                @Override
                                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        WeatherResponse weatherResponse = response.body();
                                        // Convert Kelvin to Celsius
                                        double tempInCelsius = weatherResponse.main.temp - 273.15;
                                        String weatherString = String.format("Temperature: %.2f°C\nDescription: %s",
                                                tempInCelsius, weatherResponse.weather.get(0).description);
                                        weatherInfo.setText(weatherString);
                                    }
                                }

                                @Override
                                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                                    weatherInfo.setText("Failed to get weather data");
                                }
                            });
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                weatherInfo.setText("Failed to get location data");
            }
        });
    }

    private void fetchPopulationInfo(String cityName) {
        new Thread(() -> {
            try {
                String cityCode = CityCodeMap.getCityCode(cityName);
                if (cityCode == null) {
                    Log.e("PopulationInfo", "City code not found for " + cityName);
                    return;
                }

                URL url = new URL("https://pxdata.stat.fi/PxWeb/api/v1/en/StatFin/synt/statfin_synt_pxt_12dy.px");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setDoOutput(true);

                String jsonInputString = createJsonPayload(cityCode);
                Log.d("PopulationInfo", "Sending JSON: " + jsonInputString);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                StringBuilder response = new StringBuilder();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                        String responseLine;
                        while ((responseLine = reader.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.i("PopulationInfo", "Response: " + response.toString());

                        // Parse JSON and extract population data
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONArray values = jsonResponse.getJSONArray("value");
                        final String population = values.getString(0);  // Assuming the population is the first value
                        int populationInt = Integer.parseInt(population); // Parse the population string to an integer
                        int imageResource = R.drawable.p; // Default image

                        if (populationInt >= 0 && populationInt <= 49000) {
                            imageResource = R.drawable.p;
                        } else if (populationInt >= 50000 && populationInt <= 99999) {
                            imageResource = R.drawable.p50000;
                        } else if (populationInt >= 100000 && populationInt <= 199999) {
                            imageResource = R.drawable.p100000;
                        } else if (populationInt > 200000) {
                            imageResource = R.drawable.p200000;
                        }

                        // Ana thread üzerinde ImageView ve TextView güncellemesi
                        final int finalImageResource = imageResource;
                        runOnUiThread(() -> {
                            populationInfo.setText("Population: " + population);
                            populationImage.setImageResource(finalImageResource);
                        });

                    }
                } else {
                    Log.e("PopulationInfo", "HTTP error code: " + responseCode);
                }
            } catch (IOException | JSONException | NumberFormatException e) {
                Log.e("PopulationInfo", e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }).start();
    }


    private String createJsonPayload(String cityCode) throws JSONException {
        JSONObject mainObject = new JSONObject();
        JSONArray queryArray = new JSONArray();

        JSONObject yearObject = new JSONObject();
        yearObject.put("code", "Vuosi");
        JSONObject yearSelection = new JSONObject();
        yearSelection.put("filter", "item");
        yearSelection.put("values", new JSONArray(new String[] {"2022"}));
        yearObject.put("selection", yearSelection);

        JSONObject areaObject = new JSONObject();
        areaObject.put("code", "Alue");
        JSONObject areaSelection = new JSONObject();
        areaSelection.put("filter", "item");
        areaSelection.put("values", new JSONArray(new String[] {cityCode}));
        areaObject.put("selection", areaSelection);

        JSONObject dataObject = new JSONObject();
        dataObject.put("code", "Tiedot");
        JSONObject dataSelection = new JSONObject();
        dataSelection.put("filter", "item");
        dataSelection.put("values", new JSONArray(new String[] {"vaesto"}));
        dataObject.put("selection", dataSelection);

        queryArray.put(yearObject);
        queryArray.put(areaObject);
        queryArray.put(dataObject);

        mainObject.put("query", queryArray);

        JSONObject responseObject = new JSONObject();
        responseObject.put("format", "json-stat2");
        mainObject.put("response", responseObject);

        return mainObject.toString();
    }

}


