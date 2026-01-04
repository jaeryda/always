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
        // OPTIONS 요청은 CORS preflight이므로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        
        // 공개 엔드포인트는 인증 불필요 (로그인, 회원가입, 메뉴, OpenAI API, 카카오 로그인)
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register") 
                || path.startsWith("/api/auth/kakao/login")
                || path.startsWith("/api/hello") || path.startsWith("/api/menus") 
                || path.startsWith("/api/openai/")) {
            return true;
        }

        // JWT 토큰 추출 (쿠키 또는 Authorization 헤더에서)
        String token = null;
        
        // 1. 쿠키에서 토큰 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        
        // 2. 쿠키에 없으면 Authorization 헤더에서 확인 (하위 호환성)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // "Bearer " 제거
            }
        }
        
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"인증 토큰이 필요합니다.\"}");
            return false;
        }

        try {
            // 토큰 검증
            String username = jwtService.extractUsername(token);
            if (username == null || jwtService.isTokenExpired(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"유효하지 않은 토큰입니다.\"}");
                return false;
            }

            // 토큰을 request attribute에 저장 (컨트롤러에서 사용 가능)
            request.setAttribute("userId", jwtService.extractUserId(token));
            request.setAttribute("username", username);
            
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"토큰 검증 실패: " + e.getMessage() + "\"}");
            return false;
        }
    }
}

