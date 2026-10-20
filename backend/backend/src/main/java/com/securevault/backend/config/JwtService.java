package com.securevault.backend.config;

import com.securevault.backend.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String SECRET_KEY =
            "SecureVaultSecretKeyForJwtAuthentication2026VeryStrongKey";

    private final SecretKey key =
            Keys.hmacShaKeyFor(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8)
            );

    private final long expirationTime =
            1000 * 60 * 60;

    private final long mfaTokenExpirationTime =
            1000 * 60 * 5;

    // =========================
    // NORMAL TOKEN
    // =========================

    public String generateToken(String email) {
        return generateToken(email, null, Role.USER);
    }

    public String generateToken(
            String email,
            String deviceId) {

        return generateToken(
                email,
                deviceId,
                Role.USER
        );
    }

    public String generateToken(
            String email,
            String deviceId,
            Role role) {

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim(
                        "role",
                        role == null
                                ? Role.USER.name()
                                : role.name()
                )
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationTime
                        )
                );

        if (deviceId != null
                && !deviceId.isBlank()) {

            builder.claim(
                    "device_id",
                    deviceId
            );
        }

        return builder
                .signWith(key)
                .compact();
    }

    // =========================
    // MFA TOKEN
    // =========================

    public String generateMfaToken(
            String email) {

        return generateMfaToken(
                email,
                null
        );
    }

    public String generateMfaToken(
            String email,
            String deviceId) {

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(email)
                .claim("mfa_pending", true)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + mfaTokenExpirationTime
                        )
                );

        if (deviceId != null
                && !deviceId.isBlank()) {

            builder.claim(
                    "device_id",
                    deviceId
            );
        }

        return builder
                .signWith(key)
                .compact();
    }

    // =========================
    // EXTRACT EMAIL
    // =========================

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // =========================
    // EXTRACT TOKEN ID
    // =========================

    public String extractTokenId(String token) {
        return getClaims(token).getId();
    }

    // =========================
    // EXTRACT DEVICE ID
    // =========================

    public String extractDeviceId(String token) {

        Object deviceId =
                getClaims(token)
                        .get("device_id");

        if (deviceId == null) {
            return null;
        }

        return deviceId.toString();
    }

    // =========================
    // EXTRACT ROLE
    // =========================

    public String extractRole(String token) {

        Object role =
                getClaims(token)
                        .get("role");

        if (role == null) {
            return Role.USER.name();
        }

        return role.toString();
    }

    // =========================
    // MFA TOKEN CHECK
    // =========================

    public boolean isMfaToken(String token) {

        try {

            Claims claims =
                    getClaims(token);

            Object value =
                    claims.get("mfa_pending");

            return Boolean.TRUE.equals(value);

        } catch (Exception e) {

            return false;
        }
    }

    // =========================
    // TOKEN VALIDATION
    // =========================

    public boolean isTokenValid(String token) {

        try {

            getClaims(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    // =========================
    // EXPIRATION
    // =========================

    public Date extractExpiration(
            String token) {

        return getClaims(token)
                .getExpiration();
    }

    // =========================
    // CLAIMS
    // =========================

    private Claims getClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}