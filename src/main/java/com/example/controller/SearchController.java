package com.example.controller;

import com.example.dto.LocationSearchResult;
import com.example.security.CurrentUser;
import com.example.service.LocationService;
import com.example.service.OpenWeatherClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {
    private final OpenWeatherClient weatherClient;
    private final LocationService locationService;

    public SearchController(OpenWeatherClient weatherClient, LocationService locationService) {
        this.weatherClient = weatherClient;
        this.locationService = locationService;
    }

    @GetMapping
    public String search(@RequestParam(name = "q", required = false) String q,
                         @ModelAttribute("currentUser") CurrentUser currentUser,
                         Model model) {
        if (currentUser == null) return "redirect:/login";
        model.addAttribute("q", q == null ? "" : q);
        List<LocationSearchResult> results = (q == null || q.isBlank()) ? List.of()
                : weatherClient.searchByName(q, 10);
        model.addAttribute("results", results);
        return "search/index";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("currentUser") CurrentUser currentUser,
                      @RequestParam String name,
                      @RequestParam BigDecimal lat,
                      @RequestParam BigDecimal lon) {
        if (currentUser == null) return "redirect:/login";
        locationService.add(currentUser.id(), name, lat, lon);
        return "redirect:/";
    }
}
