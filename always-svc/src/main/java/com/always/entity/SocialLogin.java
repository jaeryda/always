package com.always.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLogin {

    private Long id;

    private Long userId;  // users 테이블의 id (FK)

    private String provider;  // 소셜 로그인 제공자 (kakao, google, naver 등)

    private String providerUserId;  // 소셜 로그인 제공자의 사용자 ID (카카오 ID 등)

    private String email;  // 이메일 (users 테이블과 연결)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}

