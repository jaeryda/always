package com.always.mapper;

import com.always.entity.AppNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AppNotificationMapper {
    List<AppNotification> findByUserId(@Param("userId") Long userId);
    void insert(AppNotification notification);
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);
    int markAllAsRead(@Param("userId") Long userId);
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
    int deleteAll(@Param("userId") Long userId);
}

