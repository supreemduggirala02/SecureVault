package com.securevault.backend.controller;

import com.securevault.backend.service.PasswordGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordGeneratorController {

    private final PasswordGeneratorService passwordGeneratorService;

    public PasswordGeneratorController(
            PasswordGeneratorService passwordGeneratorService) {

        this.passwordGeneratorService = passwordGeneratorService;
    }

    @GetMapping("/generate")
    public ResponseEntity<?> generatePassword(
            @RequestParam int length,
            @RequestParam boolean uppercase,
            @RequestParam boolean lowercase,
            @RequestParam boolean numbers,
            @RequestParam boolean special,
            Authentication authentication) {

        try {
            String password =
                    passwordGeneratorService.generatePassword(
                            length,
                            uppercase,
                            lowercase,
                            numbers,
                            special
                    );

            return ResponseEntity.ok(password);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}