package com.example.dto;

import java.math.BigDecimal;

public record WeatherNow(String locationName, BigDecimal temp, String description, String icon) {
}
