package com.securevault.backend.security;

import com.securevault.backend.config.JwtService;
import com.securevault.backend.entity.Role;
import com.securevault.backend.service.DeviceService;
import com.securevault.backend.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final DeviceService deviceService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            TokenRevocationService tokenRevocationService,
            DeviceService deviceService) {

        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
        this.deviceService = deviceService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // No JWT → continue normally.
        // Public endpoints will be handled by SecurityConfig.
        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7);

        try {

            // ==================================
            // TOKEN VALIDATION
            // ==================================

            if (!jwtService.isTokenValid(token)) {

                unauthorized(
                        response,
                        "Invalid or expired token"
                );

                return;
            }

            // ==================================
            // MFA TOKEN MUST NOT ACCESS APIs
            // ==================================

            if (jwtService.isMfaToken(token)) {

                SecurityContextHolder.clearContext();

                unauthorized(
                        response,
                        "MFA verification required"
                );

                return;
            }

            // ==================================
            // TOKEN REVOCATION CHECK
            // ==================================

            String tokenId =
                    jwtService.extractTokenId(token);

            if (tokenId == null
                    || tokenId.isBlank()) {

                SecurityContextHolder.clearContext();

                unauthorized(
                        response,
                        "Invalid authentication token"
                );

                return;
            }

            if (tokenRevocationService.isRevoked(
                    tokenId)) {

                SecurityContextHolder.clearContext();

                unauthorized(
                        response,
                        "Token has been revoked"
                );

                return;
            }

            // ==================================
            // EMAIL
            // ==================================

            String email =
                    jwtService.extractEmail(token);

            if (email == null
                    || email.isBlank()) {

                SecurityContextHolder.clearContext();

                unauthorized(
                        response,
                        "Invalid authentication token"
                );

                return;
            }

            // ==================================
            // DEVICE CHECK
            // ==================================

            String deviceId =
                    jwtService.extractDeviceId(token);

            if (deviceId != null
                    && !deviceId.isBlank()) {

                boolean deviceExists =
                        deviceService.deviceExists(
                                email,
                                deviceId
                        );

                if (!deviceExists) {

                    SecurityContextHolder.clearContext();

                    unauthorized(
                            response,
                            "Device is no longer registered"
                    );

                    return;
                }

                boolean deviceRevoked =
                        deviceService.isDeviceRevoked(
                                email,
                                deviceId
                        );

                if (deviceRevoked) {

                    SecurityContextHolder.clearContext();

                    unauthorized(
                            response,
                            "Device has been revoked"
                    );

                    return;
                }
            }

            // ==================================
            // ROLE FROM JWT
            // ==================================

            String roleName =
                    jwtService.extractRole(token);

            Role role;

            try {

                role =
                        Role.valueOf(
                                roleName
                                        .trim()
                                        .toUpperCase(
                                                Locale.ROOT
                                        )
                        );

            } catch (Exception e) {

                // Unknown/invalid role
                // must never become an authority.
                SecurityContextHolder.clearContext();

                unauthorized(
                        response,
                        "Invalid authentication role"
                );

                return;
            }

            String authority =
                    "ROLE_" + role.name();

            // ==================================
            // CREATE SPRING AUTHENTICATION
            // ==================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority(
                                                authority
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }

        } catch (Exception e) {

            SecurityContextHolder.clearContext();

            unauthorized(
                    response,
                    "Invalid authentication token"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    // ==================================
    // 401 RESPONSE
    // ==================================

    private void unauthorized(
            HttpServletResponse response,
            String message)
            throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                "{\"message\":\""
                        + escapeJson(message)
                        + "\"}"
        );
    }

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}