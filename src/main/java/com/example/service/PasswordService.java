package com.example.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    public String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }

    public boolean matches(String raw, String hash) {
        return BCrypt.checkpw(raw, hash);
    }
}