package com.securevault.backend.controller;

import com.securevault.backend.dto.NotificationResponse;
import com.securevault.backend.entity.Notification;
import com.securevault.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    // =========================================================
    // GET ALL NOTIFICATIONS
    // =========================================================

    @GetMapping
    public ResponseEntity<?> getNotifications(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<Notification> notifications =
                    notificationService.getNotifications(email);

            List<NotificationResponse> response =
                    notifications.stream()
                            .map(NotificationResponse::fromEntity)
                            .toList();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            List<Notification> notifications =
                    notificationService
                            .getUnreadNotifications(email);

            List<NotificationResponse> response =
                    notifications.stream()
                            .map(NotificationResponse::fromEntity)
                            .toList();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // GET UNREAD COUNT
    // =========================================================

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            long count =
                    notificationService
                            .getUnreadCount(email);

            return ResponseEntity.ok(
                    Map.of("count", count)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            notificationService.markAsRead(
                    id,
                    email
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Notification marked as read"
                    )
            );

        } catch (RuntimeException e) {

            if ("Access denied".equals(e.getMessage())) {

                return ResponseEntity
                        .status(403)
                        .body(Map.of(
                                "message",
                                "Access denied"
                        ));
            }

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            Authentication authentication) {

        try {
            String email = authentication.getName();

            notificationService.markAllAsRead(email);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "All notifications marked as read"
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // DELETE NOTIFICATION
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            String email = authentication.getName();

            notificationService.deleteNotification(
                    id,
                    email
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Notification deleted successfully"
                    )
            );

        } catch (RuntimeException e) {

            if ("Access denied".equals(e.getMessage())) {

                return ResponseEntity
                        .status(403)
                        .body(Map.of(
                                "message",
                                "Access denied"
                        ));
            }

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }
}