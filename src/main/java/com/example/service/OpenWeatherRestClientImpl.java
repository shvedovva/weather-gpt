package com.example.service;

import com.example.dto.LocationSearchResult;
import com.example.dto.WeatherNow;
import com.example.exception.OpenWeatherException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Profile("stub-owm")
public class OpenWeatherRestClientImpl implements OpenWeatherClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String lang;
    private final String units;

    public OpenWeatherRestClientImpl(@Value("${app.openweather.base-url}") String baseUrl,
                                     @Value("${app.openweather.api-key}") String apiKey,
                                     @Value("${app.openweather.default-lang:ru}") String lang,
                                     @Value("${app.openweather.default-units:metric}") String units,
                                     @Value("${app.openweather.timeout-ms}") int timeoutMs) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.lang = lang;
        this.units = units;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public List<LocationSearchResult> searchByName(String query, int limit) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/geo/1.0/direct")
                    .queryParam("q", query)
                    .queryParam("limit", limit)
                    .queryParam("appid", apiKey);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(null),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            if (response.getStatusCode().isError()) {
                String body = response.getBody() != null ? response.getBody().toString() : "No body";
                throw new OpenWeatherException("Search error: " + response.getStatusCode() + " " + body);
            }
            List<Map<String, Object>> body = response.getBody();
            if (body == null || body.isEmpty()) {
                return List.of();
            }

            return body.stream()
                    .map(this::mapGeo)
                    .toList();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            throw new OpenWeatherException("Search error: " + e.getStatusCode() + " " + responseBody, e);
        } catch (ResourceAccessException e) {
            throw new OpenWeatherException("Network error during search: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw wrap(e);
        }
    }

    @Override
    public WeatherNow currentByCoords(BigDecimal lat, BigDecimal lon) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/data/2.5/weather")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("appid", apiKey)
                    .queryParam("units", units)
                    .queryParam("lang", lang);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            if(response.getStatusCode().isError()) {
                String body = response.getBody() != null ? response.getBody().toString() : "No body";
                throw new OpenWeatherException("Weather error: " + response.getStatusCode() + " " + body);
            }

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new OpenWeatherException("Empty response from OpenWeather");
            }

            String locName = String.valueOf(responseBody.getOrDefault("name", ""));
            Map<String, Object> main = (Map<String, Object>) responseBody.get("main");
            if (main == null) {
                throw new OpenWeatherException("Empty response from OpenWeather");
            }
            BigDecimal temp = new BigDecimal(String.valueOf(main.get("temp")));

            List<Map<String, Object>> weather = (List<Map<String, Object>>) responseBody.get("weather");
            Map<String, Object> w = weather == null || weather.isEmpty() ? Map.of() : weather.get(0);
            String description = String.valueOf(w.getOrDefault("description", ""));
            String icon = String.valueOf(w.getOrDefault("icon", ""));
            return new WeatherNow(locName, temp, description, icon);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            throw new OpenWeatherException("Weather error: " + e.getStatusCode() + " " + responseBody, e);
        } catch (ResourceAccessException e) {
            throw new OpenWeatherException("Network error during search: " + e.getMessage(), e);
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

        String display = state.isBlank()
                ? name + ", " + country
                : name + ", " + country + ", " + state;

        return new LocationSearchResult(display, lat, lon, country, state);
    }
    private OpenWeatherException wrap(RuntimeException e) {
        return (e instanceof OpenWeatherException)
                ? (OpenWeatherException) e
                : new OpenWeatherException("OpenWeather call faild", e);
    }

}
