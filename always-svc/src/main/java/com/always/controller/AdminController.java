package com.always.controller;

import com.always.entity.User;
import com.always.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.0.2:8088"})
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return false;
        Optional<User> userOpt = userService.findById(userId);
        return userOpt.map(user -> "ADMIN".equalsIgnoreCase(user.getRole())).orElse(false);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Admin only"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "admin ok"));
    }
}

