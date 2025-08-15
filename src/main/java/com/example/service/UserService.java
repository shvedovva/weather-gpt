package com.example.service;

import com.example.domain.UserEntity;
import com.example.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    public UserEntity register(String login, String rawPassword) {
        String norm = login.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByLogin(norm)) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        UserEntity user = new UserEntity();
        user.setLogin(norm);
        user.setPasswordHash(passwordService.hash(rawPassword));
        return userRepository.save(user);
    }

    public Optional<UserEntity> authenticate(String login, String rawPassword) {
        String norm = login.trim().toLowerCase(Locale.ROOT);
        return userRepository.findByLogin(norm)
                .filter(u -> passwordService.matches(rawPassword, u.getPasswordHash()));
    }
}
