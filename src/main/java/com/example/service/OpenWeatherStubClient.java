package com.example.service;

import com.example.dto.LocationSearchResult;
import com.example.dto.WeatherNow;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Profile("!stub-owm")
public class OpenWeatherStubClient implements OpenWeatherClient {

    @Override
    public List<LocationSearchResult> searchByName(String query, int limit) {
        return List.of(
                new LocationSearchResult("Санкт-Петербург, RU", new BigDecimal("59.9386"), new BigDecimal("30.3141"), "RU", "Saint-Petersburg"),
                new LocationSearchResult("Гомель, Gomel Region, BY", new BigDecimal("52.4345"), new BigDecimal("30.9754"), "BY", "Gomel Region")
        );
    }

    @Override
    public WeatherNow currentByCoords(BigDecimal lat, BigDecimal lon) {
        // Простая заглушка температуры по широте, для демонстрации
        BigDecimal temp = lat.subtract(new BigDecimal("40")).abs();
        return new WeatherNow("Stub City", temp, "ясно", "01d");
    }
}
