package com.always.service;

import com.always.entity.AIHistory;
import com.always.mapper.AIHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AIHistoryService {

    private final AIHistoryMapper aiHistoryMapper;

    @Autowired
    public AIHistoryService(AIHistoryMapper aiHistoryMapper) {
        this.aiHistoryMapper = aiHistoryMapper;
    }

    public List<AIHistory> findByUserId(Long userId) {
        return aiHistoryMapper.findByUserId(userId);
    }

    public AIHistory create(Long userId, String type, String prompt, String resultText, String resultUrl) {
        AIHistory history = new AIHistory();
        history.setUserId(userId);
        history.setType(type);
        history.setPrompt(prompt);
        history.setResultText(resultText);
        history.setResultUrl(resultUrl);
        history.setCreatedAt(LocalDateTime.now());
        aiHistoryMapper.insert(history);
        return history;
    }

    public void deleteById(Long userId, Long id) {
        aiHistoryMapper.deleteById(id, userId);
    }

    public void deleteAllByUserId(Long userId) {
        aiHistoryMapper.deleteAllByUserId(userId);
    }
}

