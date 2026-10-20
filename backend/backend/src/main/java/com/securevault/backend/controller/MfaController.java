package com.securevault.backend.controller;

import com.securevault.backend.config.JwtService;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.DeviceService;
import com.securevault.backend.service.MfaService;
import com.securevault.backend.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final DeviceService deviceService;
    private final NotificationService notificationService;

    public MfaController(
            MfaService mfaService,
            UserRepository userRepository,
            JwtService jwtService,
            DeviceService deviceService,
            NotificationService notificationService) {

        this.mfaService = mfaService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.deviceService = deviceService;
        this.notificationService = notificationService;
    }

    // =========================
    // MFA SETUP
    // =========================

    @PostMapping("/setup")
    public ResponseEntity<?> setupMfa(
            Authentication authentication) {

        try {

            if (authentication == null) {
                return ResponseEntity
                        .status(401)
                        .body("Authentication required");
            }

            String email = authentication.getName();

            User user =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            if (user.isMfaEnabled()) {
                return ResponseEntity
                        .badRequest()
                        .body("MFA is already enabled");
            }

            String secret =
                    mfaService.generateSecret();

            user.setMfaSecret(secret);

            userRepository.save(user);

            String otpUri =
                    mfaService.generateOtpUri(
                            secret,
                            user.getEmail()
                    );

            Map<String, String> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "MFA setup initialized"
            );

            response.put(
                    "email",
                    user.getEmail()
            );

            response.put(
                    "secret",
                    secret
            );

            response.put(
                    "otpUri",
                    otpUri
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // MFA VERIFY
    // =========================

    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfa(
            @RequestParam String code,
            @RequestParam(required = false) String mfaToken,
            Authentication authentication,
            HttpServletRequest request) {

        try {

            String email;
            String deviceId = null;

            // ==================================
            // LOGIN MFA FLOW
            // ==================================

            if (mfaToken != null
                    && !mfaToken.isBlank()) {

                if (!jwtService.isMfaToken(mfaToken)) {

                    return ResponseEntity
                            .status(401)
                            .body(
                                    "Invalid or expired MFA session"
                            );
                }

                email =
                        jwtService.extractEmail(
                                mfaToken
                        );

                deviceId =
                        jwtService.extractDeviceId(
                                mfaToken
                        );

                String headerDeviceId =
                        request.getHeader(
                                "X-Device-Id"
                        );

                // ==================================
                // DEVICE MATCH CHECK
                // ==================================

                if (deviceId != null
                        && headerDeviceId != null
                        && !headerDeviceId.isBlank()
                        && !deviceId.equals(
                                headerDeviceId
                        )) {

                    return ResponseEntity
                            .status(401)
                            .body(
                                    "Device mismatch"
                            );
                }

                if (deviceId == null
                        || deviceId.isBlank()) {

                    deviceId = headerDeviceId;
                }

                if (deviceId == null
                        || deviceId.isBlank()) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "Device ID is required"
                            );
                }

                if (deviceId.length() > 100) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    "Invalid device ID"
                            );
                }

                // ==================================
                // REVOKED DEVICE CHECK
                // ==================================

                if (deviceService.deviceExists(
                        email,
                        deviceId)) {

                    if (deviceService.isDeviceRevoked(
                            email,
                            deviceId)) {

                        return ResponseEntity
                                .status(401)
                                .body(
                                        "Device has been revoked"
                                );
                    }
                }

            } else {

                // ==================================
                // MFA SETUP VERIFICATION FLOW
                // ==================================

                if (authentication == null) {

                    return ResponseEntity
                            .status(401)
                            .body(
                                    "Authentication required"
                            );
                }

                email =
                        authentication.getName();
            }

            // ==================================
            // LOAD USER
            // ==================================

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "User not found"
                                    )
                            );

            // ==================================
            // ROLE SAFETY
            // ==================================

            if (user.getRole() == null) {

                user.setRole(
                        com.securevault.backend.entity.Role.USER
                );

                userRepository.save(user);
            }

            // ==================================
            // MFA SECRET CHECK
            // ==================================

            if (user.getMfaSecret() == null
                    || user.getMfaSecret().isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "MFA setup has not been initialized"
                        );
            }

            // ==================================
            // VERIFY OTP
            // ==================================

            boolean valid =
                    mfaService.verifyCode(
                            user.getMfaSecret(),
                            code
                    );

            if (!valid) {

                return ResponseEntity
                        .status(401)
                        .body(
                                "Invalid MFA code"
                        );
            }

            // ==================================
            // MFA SETUP COMPLETE
            // ==================================

            if (mfaToken == null
                    || mfaToken.isBlank()) {

                user.setMfaEnabled(true);

                userRepository.save(user);

                Map<String, Object> response =
                        new HashMap<>();

                response.put(
                        "message",
                        "MFA enabled successfully"
                );

                response.put(
                        "mfaEnabled",
                        true
                );

                response.put(
                        "role",
                        user.getRole().name()
                );

                // Notification for MFA activation
                notificationService.createNotification(
                        user.getEmail(),
                        "MFA_ENABLED",
                        "Multi-factor authentication was enabled for your account."
                );

                return ResponseEntity.ok(response);
            }

            // ==================================
            // DEVICE INFORMATION
            // ==================================

            String userAgent =
                    request.getHeader(
                            "User-Agent"
                    );

            String ipAddress =
                    getClientIp(request);

            String deviceType =
                    detectDeviceType(
                            userAgent
                    );

            String deviceName =
                    detectDeviceName(
                            userAgent
                    );

            deviceService.registerOrUpdateDevice(
                    email,
                    deviceId,
                    deviceName,
                    deviceType,
                    ipAddress,
                    userAgent
            );

            // ==================================
            // FINAL JWT WITH ROLE
            // ==================================

            String token =
                    jwtService.generateToken(
                            email,
                            deviceId,
                            user.getRole()
                    );

            // ==================================
            // MFA LOGIN SUCCESS NOTIFICATION
            // ==================================

            notificationService.createNotification(
                    user.getEmail(),
                    "MFA_LOGIN_SUCCESS",
                    "Successful MFA login detected from "
                            + deviceName
                            + "."
            );

            // ==================================
            // RESPONSE
            // ==================================

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "MFA verification successful"
            );

            response.put(
                    "mfaEnabled",
                    true
            );

            response.put(
                    "token",
                    token
            );

            response.put(
                    "email",
                    email
            );

            response.put(
                    "deviceId",
                    deviceId
            );

            response.put(
                    "role",
                    user.getRole().name()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .body(
                            "MFA verification failed"
                    );
        }
    }

    // =========================
    // DEVICE TYPE
    // =========================

    private String detectDeviceType(
            String userAgent) {

        if (userAgent == null
                || userAgent.isBlank()) {

            return "Unknown";
        }

        String ua =
                userAgent.toLowerCase();

        if (ua.contains("mobile")
                || ua.contains("android")
                || ua.contains("iphone")
                || ua.contains("ipad")) {

            return "Mobile";
        }

        if (ua.contains("windows")
                || ua.contains("macintosh")
                || ua.contains("linux")) {

            return "Desktop";
        }

        return "Unknown";
    }

    // =========================
    // DEVICE NAME
    // =========================

    private String detectDeviceName(
            String userAgent) {

        if (userAgent == null
                || userAgent.isBlank()) {

            return "Unknown Device";
        }

        String ua =
                userAgent.toLowerCase();

        if (ua.contains("iphone")) {
            return "iPhone";
        }

        if (ua.contains("ipad")) {
            return "iPad";
        }

        if (ua.contains("android")) {
            return "Android Device";
        }

        if (ua.contains("windows")) {
            return "Windows PC";
        }

        if (ua.contains("macintosh")) {
            return "Mac";
        }

        if (ua.contains("linux")) {
            return "Linux Device";
        }

        return "Unknown Device";
    }

    // =========================
    // CLIENT IP
    // =========================

    private String getClientIp(
            HttpServletRequest request) {

        String xForwardedFor =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (xForwardedFor != null
                && !xForwardedFor.isBlank()) {

            return xForwardedFor
                    .split(",")[0]
                    .trim();
        }

        String xRealIp =
                request.getHeader(
                        "X-Real-IP"
                );

        if (xRealIp != null
                && !xRealIp.isBlank()) {

            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}