package com.example.dto;

import java.math.BigDecimal;

public record WeatherCard(
        Long id,
        String savedName,
        String apiName,
        BigDecimal temp,
        String descr,
        String icon
) {
}