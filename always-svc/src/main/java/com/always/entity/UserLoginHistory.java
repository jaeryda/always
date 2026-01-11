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
public class UserLoginHistory {

    private Long id;

    private Long userId;  // users 테이블의 id (FK)

    private String actionType;  // LOGIN, LOGOUT

    private String ipAddress;  // IP 주소

    private String userAgent;  // User Agent 정보

    private String loginType;  // 로그인 타입 (email, kakao, naver, google 등)

    private LocalDateTime createdAt;

}


