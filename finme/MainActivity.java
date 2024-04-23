package com.example.finme;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchHistoryAdapter.ItemClickListener {
    private EditText cityInput;
    private Button searchButton;
    private RecyclerView searchHistoryRecyclerView;
    private SearchHistoryAdapter searchHistoryAdapter;
    private final List<String> searchHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.city_input);
        searchButton = findViewById(R.id.search_button);
        searchHistoryRecyclerView = findViewById(R.id.search_history_recyclerview);
        searchHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchHistoryAdapter = new SearchHistoryAdapter(this, searchHistory, this);
        searchHistoryRecyclerView.setAdapter(searchHistoryAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityInput.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    cityName = capitalizeCityName(cityName);
                    searchCity(cityName);
                    updateSearchHistory(cityName);
                }
            }
        });
    }

    private String capitalizeCityName(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            return cityName;
        }
        return cityName.substring(0, 1).toUpperCase() + cityName.substring(1).toLowerCase();
    }

    private void searchCity(String cityName) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("CITY_NAME", cityName);
        startActivity(intent);
    }

    private void updateSearchHistory(String cityName) {
        if (!searchHistory.contains(cityName)) {
            if (searchHistory.size() >= 10) {  // Keeping last 10 searches
                searchHistory.remove(0);
            }
            searchHistory.add(cityName);
            searchHistoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(String cityName) {
        searchCity(cityName);
    }
}
