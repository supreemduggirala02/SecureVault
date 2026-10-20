package com.securevault.backend.controller;

import com.securevault.backend.entity.Device;
import com.securevault.backend.service.DeviceService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    // =========================================================
    // GET ALL DEVICES
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getDevices(
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            List<Device> devices =
                    deviceService.getUserDevices(email);

            return ResponseEntity.ok(devices);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(401)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // REGISTER DEVICE
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<?> registerDevice(
            @RequestBody DeviceRegisterRequest request,
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            Device device =
                    deviceService.registerOrUpdateDevice(
                            email,
                            request.getDeviceId(),
                            request.getDeviceName(),
                            request.getDeviceType(),
                            request.getIpAddress(),
                            request.getUserAgent()
                    );

            return ResponseEntity.ok(device);

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


    // =========================================================
    // GET SINGLE DEVICE
    // =========================================================

    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getDevice(
            @PathVariable Long deviceId,
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            Device device =
                    deviceService.getUserDevice(
                            email,
                            deviceId
                    );

            return ResponseEntity.ok(device);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(404)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }


    // =========================================================
    // TRUST DEVICE
    // =========================================================

    @PutMapping("/{deviceId}/trust")
    public ResponseEntity<?> trustDevice(
            @PathVariable Long deviceId,
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            Device device =
                    deviceService.trustDevice(
                            email,
                            deviceId
                    );

            return ResponseEntity.ok(device);

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


    // =========================================================
    // REVOKE DEVICE
    // =========================================================

    @PutMapping("/{deviceId}/revoke")
    public ResponseEntity<?> revokeDevice(
            @PathVariable Long deviceId,
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            Device device =
                    deviceService.revokeDevice(
                            email,
                            deviceId
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Device revoked successfully",
                            "device", device
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


    // =========================================================
    // RESTORE DEVICE
    // =========================================================

    @PutMapping("/{deviceId}/restore")
    public ResponseEntity<?> restoreDevice(
            @PathVariable Long deviceId,
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            Device device =
                    deviceService.restoreDevice(
                            email,
                            deviceId
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Device restored successfully",
                            "device", device
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


    // =========================================================
    // DELETE DEVICE
    // =========================================================

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteDevice(
            @PathVariable Long deviceId,
            Authentication authentication) {

        try {

            String email =
                    getAuthenticatedEmail(authentication);

            deviceService.deleteDevice(
                    email,
                    deviceId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Device deleted successfully"
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


    // =========================================================
    // AUTHENTICATED EMAIL
    // =========================================================

    private String getAuthenticatedEmail(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new RuntimeException(
                    "Authentication required"
            );
        }

        return authentication.getName();
    }


    // =========================================================
    // REGISTER REQUEST DTO
    // =========================================================

    public static class DeviceRegisterRequest {

        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String ipAddress;
        private String userAgent;


        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }
}