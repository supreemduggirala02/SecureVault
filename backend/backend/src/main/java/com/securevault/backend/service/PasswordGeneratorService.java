package com.securevault.backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String UPPERCASE =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String LOWERCASE =
            "abcdefghijklmnopqrstuvwxyz";

    private static final String NUMBERS =
            "0123456789";

    private static final String SPECIAL =
            "!@#$%^&*()_+-=[]{}";

    private final SecureRandom random = new SecureRandom();

    public String generatePassword(
            int length,
            boolean useUppercase,
            boolean useLowercase,
            boolean useNumbers,
            boolean useSpecial) {

        if (length < 4 || length > 128) {
            throw new RuntimeException(
                    "Password length must be between 4 and 128");
        }

        if (!useUppercase && !useLowercase &&
                !useNumbers && !useSpecial) {

            throw new RuntimeException(
                    "Select at least one character type");
        }

        StringBuilder characters = new StringBuilder();
        StringBuilder password = new StringBuilder();

        // Add at least one character from each selected type
        if (useUppercase) {
            characters.append(UPPERCASE);
            password.append(randomChar(UPPERCASE));
        }

        if (useLowercase) {
            characters.append(LOWERCASE);
            password.append(randomChar(LOWERCASE));
        }

        if (useNumbers) {
            characters.append(NUMBERS);
            password.append(randomChar(NUMBERS));
        }

        if (useSpecial) {
            characters.append(SPECIAL);
            password.append(randomChar(SPECIAL));
        }

        // Fill remaining length
        while (password.length() < length) {
            password.append(
                    randomChar(characters.toString()));
        }

        return password.toString();
    }

    private char randomChar(String characters) {

        int index = random.nextInt(characters.length());

        return characters.charAt(index);
    }
}