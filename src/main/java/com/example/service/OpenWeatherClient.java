package com.example.service;

import com.example.dto.LocationSearchResult;
import com.example.dto.WeatherNow;

import java.math.BigDecimal;
import java.util.List;

public interface OpenWeatherClient {
    List<LocationSearchResult> searchByName(String query, int limit);

    WeatherNow currentByCoords(BigDecimal lat, BigDecimal lon);
}