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
public class PostReaction {
    private Long id;
    private Long postId;
    private Long userId;
    private String type; // LIKE, BOOKMARK
    private LocalDateTime createdAt;
}

