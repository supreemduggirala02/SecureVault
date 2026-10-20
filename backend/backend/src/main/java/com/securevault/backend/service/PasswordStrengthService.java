package com.securevault.backend.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordStrengthService {

    public String checkStrength(String password) {

        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }

        int score = 0;

        // Length
        if (password.length() >= 8) {
            score++;
        }

        if (password.length() >= 12) {
            score++;
        }

        // Uppercase
        if (password.matches(".*[A-Z].*")) {
            score++;
        }

        // Lowercase
        if (password.matches(".*[a-z].*")) {
            score++;
        }

        // Number
        if (password.matches(".*[0-9].*")) {
            score++;
        }

        // Special character
        if (password.matches(".*[^a-zA-Z0-9].*")) {
            score++;
        }

        if (score <= 2) {
            return "Weak - Use at least 8 characters with uppercase, lowercase, number and special character";
        } else if (score <= 4) {
            return "Medium - Add more characters and a special character";
        } else {
            return "Strong";
        }
    }
}