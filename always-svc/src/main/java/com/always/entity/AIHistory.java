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
public class AIHistory {
    private Long id;
    private Long userId;
    private String type; // chat, image, video
    private String prompt;
    private String resultText;
    private String resultUrl;
    private LocalDateTime createdAt;
}

