package com.always.service;

import com.always.entity.AppNotification;
import com.always.mapper.AppNotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppNotificationService {

    private final AppNotificationMapper appNotificationMapper;

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
}

