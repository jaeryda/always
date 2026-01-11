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
        
        System.out.println("=== [JwtAuthInterceptor] 요청 처리 시작 ===");
        System.out.println("[JwtAuthInterceptor] Method: " + method + ", Path: " + path);
        
        // OPTIONS 요청은 CORS preflight이므로 통과
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("[JwtAuthInterceptor] OPTIONS 요청 - 통과");
            return true;
        }

        // 공개 엔드포인트는 인증 불필요 (로그인, 회원가입, 메뉴, OpenAI API, 카카오/네이버 로그인)
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register") 
                || path.startsWith("/api/auth/kakao/callback") || path.startsWith("/api/auth/kakao/register")
                || path.startsWith("/api/auth/kakao/login") 
                || path.startsWith("/api/auth/naver/callback") || path.startsWith("/api/auth/naver/register")
                || path.startsWith("/api/hello") 
                || path.startsWith("/api/menus") || path.startsWith("/api/openai/")) {
            System.out.println("[JwtAuthInterceptor] 공개 엔드포인트 - 인증 불필요, 통과");
            return true;
        }

        // JWT 토큰 추출 (쿠키 또는 Authorization 헤더에서)
        String token = null;
        
        // 1. 쿠키에서 토큰 확인
        Cookie[] cookies = request.getCookies();
        System.out.println("[JwtAuthInterceptor] 쿠키 개수: " + (cookies != null ? cookies.length : 0));
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("[JwtAuthInterceptor] 쿠키 이름: " + cookie.getName());
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    System.out.println("[JwtAuthInterceptor] auth_token 쿠키 발견: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
                    break;
                }
            }
        }
        
        // 2. 쿠키에 없으면 Authorization 헤더에서 확인 (하위 호환성)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            System.out.println("[JwtAuthInterceptor] Authorization 헤더: " + (authHeader != null ? "있음" : "없음"));
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // "Bearer " 제거
                System.out.println("[JwtAuthInterceptor] Authorization 헤더에서 토큰 추출");
            }
        }
        
        if (token == null || token.isEmpty()) {
            System.out.println("[JwtAuthInterceptor] ❌ 토큰이 없습니다. 401 오류 반환");
            System.out.println("[JwtAuthInterceptor] 요청 헤더:");
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println("[JwtAuthInterceptor]   " + headerName + ": " + request.getHeader(headerName));
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"인증 토큰이 필요합니다.2\"}");
            return false;
        }

        try {
            System.out.println("[JwtAuthInterceptor] 토큰 검증 시작");
            // 토큰 검증
            String username = jwtService.extractUsername(token);
            System.out.println("[JwtAuthInterceptor] 추출된 username: " + username);
            
            boolean isExpired = jwtService.isTokenExpired(token);
            System.out.println("[JwtAuthInterceptor] 토큰 만료 여부: " + isExpired);
            
            if (username == null || isExpired) {
                System.out.println("[JwtAuthInterceptor] ❌ 유효하지 않은 토큰");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"유효하지 않은 토큰입니다.2\"}");
                return false;
            }

            // 토큰을 request attribute에 저장 (컨트롤러에서 사용 가능)
            Long userId = jwtService.extractUserId(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            
            System.out.println("[JwtAuthInterceptor] ✅ 토큰 검증 성공 - userId: " + userId + ", username: " + username);
            System.out.println("=== [JwtAuthInterceptor] 요청 처리 완료 ===");
            return true;
        } catch (Exception e) {
            System.out.println("[JwtAuthInterceptor] ❌ 토큰 검증 예외: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"토큰 검증 실패: " + e.getMessage() + "\"}");
            return false;
        }
    }
}

