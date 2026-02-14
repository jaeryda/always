package com.always.controller;

import com.always.entity.AIHistory;
import com.always.service.AIHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-history")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.0.2:8088"})
public class AIHistoryController {

    private final AIHistoryService aiHistoryService;

    @Autowired
    public AIHistoryController(AIHistoryService aiHistoryService) {
        this.aiHistoryService = aiHistoryService;
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        List<AIHistory> items = aiHistoryService.findByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true, "items", items));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));

        AIHistory item = aiHistoryService.create(
                userId,
                body.getOrDefault("type", "chat"),
                body.getOrDefault("prompt", ""),
                body.get("resultText"),
                body.get("resultUrl")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "item", item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        aiHistoryService.deleteById(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        aiHistoryService.deleteAllByUserId(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

