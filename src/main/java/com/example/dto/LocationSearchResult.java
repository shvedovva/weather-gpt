package com.example.dto;

import java.math.BigDecimal;

public record LocationSearchResult(String displayName,
                                   BigDecimal lat,
                                   BigDecimal lon,
                                   String country,
                                   String state) {}
