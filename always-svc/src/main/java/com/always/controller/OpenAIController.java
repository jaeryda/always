package com.always.controller;

import com.always.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/openai")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.75.85:8088"})
public class OpenAIController {

    @Autowired
    private OpenAIService openAIService;

    @PostMapping("/completion")
    public ResponseEntity<Map<String, Object>> generateCompletion(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String prompt = (String) request.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "프롬프트를 입력해주세요.");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.badRequest().body(response);
            }

            Integer maxTokens = request.get("maxTokens") != null ? 
                ((Number) request.get("maxTokens")).intValue() : null;
            Double temperature = request.get("temperature") != null ? 
                ((Number) request.get("temperature")).doubleValue() : null;

            String completion = openAIService.generateCompletion(prompt, maxTokens, temperature);

            response.put("success", true);
            response.put("message", "완료되었습니다.");
            response.put("result", completion);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "OpenAI API 호출 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

