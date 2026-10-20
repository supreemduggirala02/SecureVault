package com.securevault.backend.controller;

import com.securevault.backend.config.JwtService;
import com.securevault.backend.entity.Role;
import com.securevault.backend.entity.User;
import com.securevault.backend.service.AuditLogService;
import com.securevault.backend.service.DeviceService;
import com.securevault.backend.service.NotificationService;
import com.securevault.backend.service.TokenRevocationService;
import com.securevault.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final AuditLogService auditLogService;
    private final DeviceService deviceService;
    private final NotificationService notificationService;

    public AuthController(
            UserService userService,
            JwtService jwtService,
            TokenRevocationService tokenRevocationService,
            AuditLogService auditLogService,
            DeviceService deviceService,
            NotificationService notificationService) {

        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
        this.auditLogService = auditLogService;
        this.deviceService = deviceService;
        this.notificationService = notificationService;
    }

    // =========================
    // REGISTER
    // =========================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user) {

        try {

            User registeredUser =
                    userService.registerUser(user);

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "message",
                    "User registered successfully"
            );

            response.put(
                    "email",
                    registeredUser.getEmail()
            );

            response.put(
                    "role",
                    registeredUser.getRole().name()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================
    // LOGIN
    // =========================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User user,
            HttpServletRequest request) {

        String email = user.getEmail();

        String ipAddress =
                getClientIp(request);

        String userAgent =
                request.getHeader("User-Agent");

        try {

            User loggedInUser =
                    userService.loginUser(
                            user.getEmail(),
                            user.getPassword()
                    );

            // Safety for old users
            if (loggedInUser.getRole() == null) {

                loggedInUser.setRole(Role.USER);

                userService.saveUser(loggedInUser);
            }

            String deviceId =
                    request.getHeader("X-Device-Id");

            if (deviceId == null
                    || deviceId.isBlank()) {

                deviceId =
                        UUID.randomUUID().toString();
            }

            if (deviceId.length() > 100) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid device ID"
                                )
                        );
            }

            String deviceType =
                    detectDeviceType(userAgent);

            String deviceName =
                    detectDeviceName(userAgent);

            // =========================
            // DEVICE REVOCATION CHECK
            // =========================

            if (deviceService.deviceExists(
                    loggedInUser.getEmail(),
                    deviceId)) {

                if (deviceService.isDeviceRevoked(
                        loggedInUser.getEmail(),
                        deviceId)) {

                    auditLogService.logLogin(
                            loggedInUser.getEmail(),
                            "LOGIN_FAILED",
                            ipAddress,
                            userAgent,
                            "Login rejected because device is revoked"
                    );

                    return ResponseEntity
                            .status(401)
                            .body(
                                    Map.of(
                                            "message",
                                            "Device has been revoked"
                                    )
                            );
                }
            }

            // =========================
            // MFA REQUIRED
            // =========================

            if (loggedInUser.isMfaEnabled()) {

                String mfaToken =
                        jwtService.generateMfaToken(
                                loggedInUser.getEmail(),
                                deviceId
                        );

                auditLogService.logLogin(
                        loggedInUser.getEmail(),
                        "MFA_REQUIRED",
                        ipAddress,
                        userAgent,
                        "Password correct, MFA verification required"
                );

                return ResponseEntity.ok(
                        new LoginResponse(
                                "MFA verification required",
                                null,
                                loggedInUser.getEmail(),
                                true,
                                mfaToken,
                                loggedInUser.getRole().name()
                        )
                );
            }

            // =========================
            // REGISTER DEVICE
            // =========================

            deviceService.registerOrUpdateDevice(
                    loggedInUser.getEmail(),
                    deviceId,
                    deviceName,
                    deviceType,
                    ipAddress,
                    userAgent
            );

            // =========================
            // GENERATE JWT WITH ROLE
            // =========================

            String token =
                    jwtService.generateToken(
                            loggedInUser.getEmail(),
                            deviceId,
                            loggedInUser.getRole()
                    );

            auditLogService.logLogin(
                    loggedInUser.getEmail(),
                    "LOGIN_SUCCESS",
                    ipAddress,
                    userAgent,
                    "Successful login"
            );

            // =========================
            // CREATE LOGIN NOTIFICATION
            // =========================

            notificationService.createNotification(
                    loggedInUser.getEmail(),
                    "LOGIN_SUCCESS",
                    "Successful login detected from "
                            + deviceName
                            + "."
            );

            return ResponseEntity.ok(
                    new LoginResponse(
                            "Login successful",
                            token,
                            loggedInUser.getEmail(),
                            false,
                            null,
                            loggedInUser.getRole().name()
                    )
            );

        } catch (RuntimeException e) {

            auditLogService.logLogin(
                    email,
                    "LOGIN_FAILED",
                    ipAddress,
                    userAgent,
                    "Failed login attempt"
            );

            return ResponseEntity
                    .status(401)
                    .body(e.getMessage());
        }
    }

    // =========================
    // LOGOUT
    // =========================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request) {

        String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Authorization token is required"
                            )
                    );
        }

        String token =
                authHeader.substring(7);

        try {

            if (!jwtService.isTokenValid(token)) {

                return ResponseEntity
                        .status(401)
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid or expired token"
                                )
                        );
            }

            if (jwtService.isMfaToken(token)) {

                return ResponseEntity
                        .status(401)
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid authentication token"
                                )
                        );
            }

            String tokenId =
                    jwtService.extractTokenId(token);

            long expirationTime =
                    jwtService
                            .extractExpiration(token)
                            .getTime();

            tokenRevocationService.revokeToken(
                    tokenId,
                    expirationTime
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Logout successful"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    "Invalid authentication token"
                            )
                    );
        }
    }

    // =========================
    // FORGOT PASSWORD
    // =========================

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody Map<String, String> request) {

        String email =
                request.get("email");

        if (email == null || email.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Email is required"
                            )
                    );
        }

        try {

            User user =
                    userService.getUserByEmail(email);

            String resetToken =
                    UUID.randomUUID().toString();

            LocalDateTime expiry =
                    LocalDateTime
                            .now()
                            .plusMinutes(15);

            user.setResetToken(resetToken);
            user.setResetTokenExpiry(expiry);

            userService.saveUser(user);

            // Password recovery notification
            notificationService.createNotification(
                    user.getEmail(),
                    "PASSWORD_RESET_REQUEST",
                    "A password reset request was created for your account. "
                            + "The reset token expires in 15 minutes."
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Password reset token generated",
                            "resetToken",
                            resetToken
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================
    // RESET PASSWORD
    // =========================

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody Map<String, String> request) {

        String resetToken =
                request.get("resetToken");

        String newPassword =
                request.get("newPassword");

        if (resetToken == null
                || resetToken.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "Reset token is required"
                            )
                    );
        }

        if (newPassword == null
                || newPassword.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    "New password is required"
                            )
                    );
        }

        try {

            User user =
                    userService
                            .getUserByResetToken(
                                    resetToken
                            );

            if (user == null) {

                return ResponseEntity
                        .status(400)
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid reset token"
                                )
                        );
            }

            if (user.getResetTokenExpiry() == null
                    || user.getResetTokenExpiry()
                    .isBefore(LocalDateTime.now())) {

                return ResponseEntity
                        .status(400)
                        .body(
                                Map.of(
                                        "message",
                                        "Reset token has expired"
                                )
                        );
            }

            if (newPassword.length() < 8) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Password must be at least 8 characters long"
                                )
                        );
            }

            userService.updatePassword(
                    user,
                    newPassword
            );

            user.setResetToken(null);
            user.setResetTokenExpiry(null);

            userService.saveUser(user);

            // Password changed notification
            notificationService.createNotification(
                    user.getEmail(),
                    "PASSWORD_RESET_SUCCESS",
                    "Your SecureVault password was changed successfully."
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Password reset successful"
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
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
                request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null
                && !xForwardedFor.isBlank()) {

            return xForwardedFor
                    .split(",")[0]
                    .trim();
        }

        String xRealIp =
                request.getHeader("X-Real-IP");

        if (xRealIp != null
                && !xRealIp.isBlank()) {

            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // =========================
    // LOGIN RESPONSE
    // =========================

    public static class LoginResponse {

        private String message;
        private String token;
        private String email;
        private boolean mfaRequired;
        private String mfaToken;
        private String role;

        public LoginResponse(
                String message,
                String token,
                String email,
                boolean mfaRequired,
                String mfaToken,
                String role) {

            this.message = message;
            this.token = token;
            this.email = email;
            this.mfaRequired = mfaRequired;
            this.mfaToken = mfaToken;
            this.role = role;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isMfaRequired() {
            return mfaRequired;
        }

        public void setMfaRequired(
                boolean mfaRequired) {

            this.mfaRequired = mfaRequired;
        }

        public String getMfaToken() {
            return mfaToken;
        }

        public void setMfaToken(String mfaToken) {
            this.mfaToken = mfaToken;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}