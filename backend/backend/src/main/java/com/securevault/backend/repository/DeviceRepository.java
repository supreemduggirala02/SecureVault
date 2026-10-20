package com.securevault.backend.repository;

import com.securevault.backend.entity.Device;
import com.securevault.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByUserAndDeviceId(User user, String deviceId);

    List<Device> findAllByUserOrderByLastSeenAtDesc(User user);

    Optional<Device> findByIdAndUser(Long id, User user);

    boolean existsByUserAndDeviceId(User user, String deviceId);
}