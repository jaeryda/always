package com.always.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret:always-secret-key-change-in-production-min-256-bits}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24시간 (밀리초)
    private Long expiration;

    public String getSecret() {
        return secret;
    }

    public Long getExpiration() {
        return expiration;
    }
}

