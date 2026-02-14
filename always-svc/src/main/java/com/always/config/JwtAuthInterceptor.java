package com.always.config;

import com.always.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Public endpoints
        if (path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/kakao/callback")
                || path.startsWith("/api/auth/kakao/register")
                || path.startsWith("/api/auth/kakao/login")
                || path.startsWith("/api/auth/naver/callback")
                || path.startsWith("/api/auth/naver/register")
                || path.startsWith("/api/hello")
                || path.startsWith("/api/openai/")) {
            return true;
        }

        // Menus GET is public, mutation requires auth
        if (path.startsWith("/api/menus") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        String token = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"인증 토큰이 필요합니다\"}");
            return false;
        }

        try {
            String username = jwtService.extractUsername(token);
            boolean isExpired = jwtService.isTokenExpired(token);

            if (username == null || isExpired) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"유효하지 않은 토큰입니다\"}");
                return false;
            }

            Long userId = jwtService.extractUserId(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"토큰 검증 실패\"}");
            return false;
        }
    }
}
