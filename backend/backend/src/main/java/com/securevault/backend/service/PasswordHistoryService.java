package com.securevault.backend.service;

import com.securevault.backend.entity.Credential;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PasswordHistoryService {

    private final Map<Long, List<String>> passwordHistory =
            new HashMap<>();

    // Save old password
    public void savePasswordHistory(
            Credential credential,
            String oldPassword) {

        if (credential == null ||
                credential.getId() == null ||
                oldPassword == null ||
                oldPassword.isBlank()) {

            return;
        }

        Long credentialId = credential.getId();

        passwordHistory
                .computeIfAbsent(
                        credentialId,
                        key -> new ArrayList<>()
                )
                .add(oldPassword);
    }

    // Get password history
    public List<String> getPasswordHistory(
            Credential credential) {

        if (credential == null ||
                credential.getId() == null) {

            return new ArrayList<>();
        }

        return new ArrayList<>(
                passwordHistory.getOrDefault(
                        credential.getId(),
                        new ArrayList<>()
                )
        );
    }

    // Check whether password was used before
    public boolean wasPasswordUsedBefore(
            Credential credential,
            String password) {

        if (credential == null ||
                credential.getId() == null ||
                password == null) {

            return false;
        }

        return passwordHistory
                .getOrDefault(
                        credential.getId(),
                        new ArrayList<>()
                )
                .contains(password);
    }

    // Clear history
    public void clearHistory(
            Credential credential) {

        if (credential == null ||
                credential.getId() == null) {

            return;
        }

        passwordHistory.remove(
                credential.getId()
        );
    }
}