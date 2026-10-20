package com.securevault.backend.service;

import com.securevault.backend.entity.Notification;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.NotificationRepository;
import com.securevault.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification createNotification(
            String email,
            String type,
            String message) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (type == null || type.isBlank()) {
            throw new RuntimeException("Notification type is required");
        }

        if (message == null || message.isBlank()) {
            throw new RuntimeException("Notification message is required");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type.trim());
        notification.setMessage(message.trim());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository
                .findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAsRead(Long id, String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications =
                notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Long id, String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        notificationRepository.delete(notification);
    }
}