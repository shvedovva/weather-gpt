package com.example.controller;

import com.example.domain.LocationEntity;
import com.example.dto.WeatherCard;
import com.example.dto.WeatherNow;
import com.example.security.CurrentUser;
import com.example.service.LocationService;
import com.example.service.OpenWeatherClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    private final LocationService locationService;
    private final OpenWeatherClient weatherClient;

    public HomeController(LocationService locationService, OpenWeatherClient weatherClient) {
        this.locationService = locationService;
        this.weatherClient = weatherClient;
    }

    @GetMapping("/")
    public String home(@ModelAttribute("currentUser") CurrentUser currentUser, Model model) {
        if (currentUser == null) {
            return "home/landing";
        }
        List<LocationEntity> locs = locationService.list(currentUser.id());

        List<WeatherCard> cards = locs.stream().map(loc -> {
            WeatherNow w = weatherClient.currentByCoords(loc.getLatitude(), loc.getLongitude());
            return new WeatherCard(
                    loc.getId(),
                    loc.getName(),
                    w.locationName(),
                    w.temp(),
                    w.description(),
                    w.icon()
            );
        }).toList();
        model.addAttribute("locations", cards);
        return "home/index";
    }

    @PostMapping("/locations/{id}/delete")
    public String delete(@ModelAttribute("currentUser") CurrentUser currentUser, @PathVariable Long id) {
        if (currentUser == null) return "redirect:/login";
        locationService.remove(currentUser.id(), id);
        return "redirect:/";
    }
}
