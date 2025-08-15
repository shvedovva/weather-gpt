package com.example.service;

import com.example.domain.SessionEntity;
import com.example.domain.UserEntity;
import com.example.repo.SessionRepository;
import com.example.repo.UserRepository;
import com.example.security.CurrentUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SessionService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final String cookieName;
    private final boolean cookieSecure;
    private final Duration duration;

    public SessionService(SessionRepository sessionRepository,
                          UserRepository userRepository,
                          @Value("${app.session.cookie-name}") String cookieName,
                          @Value("${app.session.secure:false}") boolean cookieSecure,
                          @Value("${app.session.duration-hours}") long hours) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.duration = Duration.ofHours(hours);
    }

    public Cookie createSessionAndCookie(Long userId) {
        UserEntity user = userRepository.getReferenceById(userId);
        SessionEntity s = new SessionEntity();
        s.setId(UUID.randomUUID());
        s.setUser(user);
        s.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plus(duration));
        sessionRepository.save(s);

        Cookie cookie = new Cookie(cookieName, s.getId().toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge((int) duration.toSeconds());
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    public Optional<CurrentUser> resolveCurrentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                try {
                    UUID id = UUID.fromString(c.getValue());
                    return sessionRepository.findActiveById(id, OffsetDateTime.now(ZoneOffset.UTC))
                            .map(s -> new CurrentUser(s.getUser().getId(), s.getUser().getLogin()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return Optional.empty();
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> cookieName.equals(c.getName()))
                .findFirst()
                .ifPresent(c -> {
                    try {
                        UUID id = UUID.fromString(c.getValue());
                        sessionRepository.deleteById(id);
                    } catch (Exception ignored) {
                    }
                    Cookie expired = new Cookie(cookieName, "");
                    expired.setPath("/");
                    expired.setMaxAge(0);
                    expired.setHttpOnly(true);
                    expired.setSecure(cookieSecure);
                    expired.setAttribute("SameSite", "Lax");
                    response.addCookie(expired);
                });
    }

    public int purgeExpired() {
        return sessionRepository.deleteExpired(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
