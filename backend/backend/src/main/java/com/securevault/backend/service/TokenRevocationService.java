package com.securevault.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    private final Map<String, Long> revokedTokens =
            new ConcurrentHashMap<>();

    public void revokeToken(
            String tokenId,
            long expirationTime) {

        revokedTokens.put(
                tokenId,
                expirationTime
        );
    }

    public boolean isRevoked(String tokenId) {

        Long expirationTime =
                revokedTokens.get(tokenId);

        if (expirationTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expirationTime) {

            revokedTokens.remove(tokenId);

            return false;
        }

        return true;
    }
}