package com.always.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppNotification {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}

