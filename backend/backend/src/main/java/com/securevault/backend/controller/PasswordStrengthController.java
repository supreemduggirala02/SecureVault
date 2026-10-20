package com.securevault.backend.controller;

import com.securevault.backend.service.PasswordStrengthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordStrengthController {

    private final PasswordStrengthService passwordStrengthService;

    public PasswordStrengthController(
            PasswordStrengthService passwordStrengthService) {

        this.passwordStrengthService = passwordStrengthService;
    }

    @GetMapping("/strength")
    public ResponseEntity<?> checkStrength(
            @RequestParam String password,
            Authentication authentication) {

        String strength =
                passwordStrengthService.checkStrength(password);

        return ResponseEntity.ok(strength);
    }
}