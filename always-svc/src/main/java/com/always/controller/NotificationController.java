package com.always.controller;

import com.always.entity.AppNotification;
import com.always.service.AppNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.0.2:8088"})
public class NotificationController {

    private final AppNotificationService appNotificationService;

    @Autowired
    public NotificationController(AppNotificationService appNotificationService) {
        this.appNotificationService = appNotificationService;
    }

    private Long requireUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        List<AppNotification> notifications = appNotificationService.findByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "notifications", notifications));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        AppNotification notification = appNotificationService.create(userId, body.getOrDefault("title", "알림"), body.getOrDefault("message", ""));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "notification", notification));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        appNotificationService.markAsRead(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        appNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        appNotificationService.deleteById(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll(HttpServletRequest request) {
        Long userId = requireUserId(request);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        appNotificationService.deleteAll(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

