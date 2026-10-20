package com.securevault.backend.service;

import com.securevault.backend.entity.Device;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.DeviceRepository;
import com.securevault.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceService(
            DeviceRepository deviceRepository,
            UserRepository userRepository) {

        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }


    // =========================================================
    // REGISTER / UPDATE DEVICE
    // =========================================================

    @Transactional
    public Device registerOrUpdateDevice(
            String email,
            String deviceId,
            String deviceName,
            String deviceType,
            String ipAddress,
            String userAgent) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (deviceId == null || deviceId.isBlank()) {
            throw new RuntimeException("Device ID is required");
        }

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        Device device =
                deviceRepository
                        .findByUserAndDeviceId(user, deviceId)
                        .orElse(null);


        // -----------------------------------------------------
        // NEW DEVICE
        // -----------------------------------------------------

        if (device == null) {

            device = new Device();

            device.setUser(user);
            device.setDeviceId(deviceId);
            device.setTrusted(false);
            device.setRevoked(false);

        } else {

            // -------------------------------------------------
            // REVOKED DEVICE
            // -------------------------------------------------

            if (device.isRevoked()) {

                throw new RuntimeException(
                        "Device has been revoked"
                );
            }
        }


        // -----------------------------------------------------
        // UPDATE DEVICE INFORMATION
        // -----------------------------------------------------

        device.setDeviceName(deviceName);
        device.setDeviceType(deviceType);
        device.setIpAddress(ipAddress);
        device.setUserAgent(userAgent);
        device.setLastSeenAt(LocalDateTime.now());

        return deviceRepository.save(device);
    }


    // =========================================================
    // GET ALL USER DEVICES
    // =========================================================

    @Transactional(readOnly = true)
    public List<Device> getUserDevices(
            String email) {

        User user =
                getUser(email);

        return deviceRepository
                .findAllByUserOrderByLastSeenAtDesc(user);
    }


    // =========================================================
    // GET SINGLE USER DEVICE
    // =========================================================

    @Transactional(readOnly = true)
    public Device getUserDevice(
            String email,
            Long deviceId) {

        User user =
                getUser(email);

        return deviceRepository
                .findByIdAndUser(deviceId, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Device not found"
                        )
                );
    }


    // =========================================================
    // TRUST DEVICE
    // =========================================================

    @Transactional
    public Device trustDevice(
            String email,
            Long deviceId) {

        Device device =
                getUserDevice(email, deviceId);

        if (device.isRevoked()) {

            throw new RuntimeException(
                    "Revoked device cannot be trusted"
            );
        }

        device.setTrusted(true);

        return deviceRepository.save(device);
    }


    // =========================================================
    // REVOKE DEVICE
    // =========================================================

    @Transactional
    public Device revokeDevice(
            String email,
            Long deviceId) {

        Device device =
                getUserDevice(email, deviceId);

        device.setRevoked(true);
        device.setTrusted(false);

        return deviceRepository.save(device);
    }


    // =========================================================
    // RESTORE DEVICE
    // =========================================================

    @Transactional
    public Device restoreDevice(
            String email,
            Long deviceId) {

        Device device =
                getUserDevice(email, deviceId);

        device.setRevoked(false);

        return deviceRepository.save(device);
    }


    // =========================================================
    // DELETE DEVICE
    // =========================================================

    @Transactional
    public void deleteDevice(
            String email,
            Long deviceId) {

        Device device =
                getUserDevice(email, deviceId);

        deviceRepository.delete(device);
    }


    // =========================================================
    // CHECK DEVICE EXISTS
    // =========================================================

    @Transactional(readOnly = true)
    public boolean deviceExists(
            String email,
            String deviceId) {

        if (email == null || email.isBlank()
                || deviceId == null || deviceId.isBlank()) {

            return false;
        }

        User user =
                userRepository.findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return false;
        }

        return deviceRepository
                .findByUserAndDeviceId(user, deviceId)
                .isPresent();
    }


    // =========================================================
    // CHECK DEVICE REVOKED
    // =========================================================

    @Transactional(readOnly = true)
    public boolean isDeviceRevoked(
            String email,
            String deviceId) {

        if (email == null || email.isBlank()
                || deviceId == null || deviceId.isBlank()) {

            return true;
        }

        User user =
                userRepository.findByEmail(email)
                        .orElse(null);

        if (user == null) {
            return true;
        }

        Device device =
                deviceRepository
                        .findByUserAndDeviceId(
                                user,
                                deviceId
                        )
                        .orElse(null);

        if (device == null) {
            return true;
        }

        return device.isRevoked();
    }


    // =========================================================
    // INTERNAL USER LOOKUP
    // =========================================================

    private User getUser(String email) {

        if (email == null || email.isBlank()) {

            throw new RuntimeException(
                    "Email is required"
            );
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }
}