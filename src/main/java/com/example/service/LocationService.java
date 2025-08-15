package com.example.service;

import com.example.domain.LocationEntity;
import com.example.domain.UserEntity;
import com.example.repo.LocationRepository;
import com.example.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class LocationService {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public LocationService(LocationRepository locationRepository, UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    public List<LocationEntity> list(Long userId) {
        return locationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public LocationEntity add(Long userId, String name, BigDecimal lat, BigDecimal lon) {
        UserEntity user = userRepository.getReferenceById(userId);
        LocationEntity e = new LocationEntity();
        e.setUser(user);
        e.setName(name);
        e.setLatitude(lat);
        e.setLongitude(lon);
        return locationRepository.save(e);
    }

    public void remove(Long userId, Long locationId) {
        LocationEntity loc = locationRepository.findByIdAndUserId(locationId, userId)
                .orElseThrow(() -> new RuntimeException("Локация не найдена или доступ запрещён"));
        locationRepository.delete(loc);
    }
}
