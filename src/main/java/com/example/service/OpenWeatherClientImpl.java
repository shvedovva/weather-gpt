package com.example.service;

import com.example.dto.LocationSearchResult;
import com.example.dto.WeatherNow;
import com.example.exception.OpenWeatherException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Profile("stub-owm")
public class OpenWeatherClientImpl implements OpenWeatherClient {
    private final WebClient webClient;
    private final String apiKey;
    private final String lang;
    private final String units;

    public OpenWeatherClientImpl(@Value("${app.openweather.base-url}") String baseUrl,
                                 @Value("${app.openweather.api-key}") String apiKey,
                                 @Value("${app.openweather.default-lang:ru}") String lang,
                                 @Value("${app.openweather.default-units:metric}") String units,
                                 @Value("${app.openweather.timeout-ms:3000}") int timeoutMs,
                                 ObjectMapper mapper) {
        this.apiKey = apiKey;
        this.lang = lang;
        this.units = units;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofMillis(timeoutMs))
                ))
                .codecs(cfg -> cfg.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper)))
                .build();
    }

    @Override
    public List<LocationSearchResult> searchByName(String query, int limit) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/geo/1.0/direct")
                            .queryParam("q", query)
                            .queryParam("limit", limit)
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new OpenWeatherException("Search error: " + resp.statusCode() + " " + body))))
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .map(list -> list.stream().map(this::mapGeo).toList())
                    .block();
        } catch (RuntimeException e) {
            throw wrap(e);
        }
    }

    private LocationSearchResult mapGeo(Map<String, Object> m) {
        BigDecimal lat = new BigDecimal(String.valueOf(m.get("lat")));
        BigDecimal lon = new BigDecimal(String.valueOf(m.get("lon")));
        String name = String.valueOf(m.get("name"));
        String country = String.valueOf(m.getOrDefault("country", ""));
        String state = String.valueOf(m.getOrDefault("state", ""));
        String display = state.isBlank() ? name + ", " + country : name + ", " + state + ", " + country;
        return new LocationSearchResult(display, lat, lon, country, state);
    }

    @Override
    public WeatherNow currentByCoords(BigDecimal lat, BigDecimal lon) {
        try {
            Map<String, Object> resp = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/data/2.5/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("lang", lang)
                            .queryParam("units", units)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new OpenWeatherException("Weather error: " + r.statusCode() + " " + body))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            String locName = String.valueOf(resp.getOrDefault("name", ""));
            Map<String, Object> main = (Map<String, Object>) resp.get("main");
            BigDecimal temp = new BigDecimal(String.valueOf(main.get("temp")));
            List<Map<String, Object>> weather = (List<Map<String, Object>>) resp.get("weather");
            Map<String, Object> w = weather.isEmpty() ? Map.of() : weather.get(0);
            String description = String.valueOf(w.getOrDefault("description", ""));
            String icon = String.valueOf(w.getOrDefault("icon", ""));
            return new WeatherNow(locName, temp, description, icon);
        } catch (RuntimeException e) {
            throw wrap(e);
        }
    }

    private OpenWeatherException wrap(RuntimeException e) {
        return (e instanceof OpenWeatherException) ? (OpenWeatherException) e
                : new OpenWeatherException("OpenWeather call failed", e);
    }
}