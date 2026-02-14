package com.always.service;

import com.always.entity.AppNotification;
import com.always.mapper.AppNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class AppNotificationService {

    private final AppNotificationMapper appNotificationMapper;
    private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    @Autowired
    public AppNotificationService(AppNotificationMapper appNotificationMapper) {
        this.appNotificationMapper = appNotificationMapper;
    }

    public List<AppNotification> findByUserId(Long userId) {
        return appNotificationMapper.findByUserId(userId);
    }

    public AppNotification create(Long userId, String title, String message) {
        AppNotification notification = new AppNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        appNotificationMapper.insert(notification);
        emitToUser(userId, notification);
        return notification;
    }

    public void markAsRead(Long userId, Long id) {
        appNotificationMapper.markAsRead(id, userId);
    }

    public void markAllAsRead(Long userId) {
        appNotificationMapper.markAllAsRead(userId);
    }

    public void deleteById(Long userId, Long id) {
        appNotificationMapper.deleteById(id, userId);
    }

    public void deleteAll(Long userId) {
        appNotificationMapper.deleteAll(userId);
    }

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emittersByUser.computeIfAbsent(userId, key -> new ArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((ex) -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) return;
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId);
        }
    }

    private void emitToUser(Long userId, AppNotification notification) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }

        if (!dead.isEmpty()) {
            emitters.removeAll(dead);
            if (emitters.isEmpty()) {
                emittersByUser.remove(userId);
            }
        }
    }
}
