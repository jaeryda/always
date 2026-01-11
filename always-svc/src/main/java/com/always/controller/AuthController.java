package com.always.controller;

import com.always.entity.User;
import com.always.service.JwtService;
import com.always.service.UserService;
import com.always.service.KakaoApiService;
import com.always.service.NaverApiService;
import com.always.service.UserLoginHistoryService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.75.80:8088"})
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private KakaoApiService kakaoApiService;

    @Autowired
    private NaverApiService naverApiService;

    @Autowired
    private UserLoginHistoryService userLoginHistoryService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "입력 정보를 확인해주세요.");
            response.put("errors", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User savedUser = userService.register(user);
            response.put("success", true);
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail()
            ));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원가입 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "사용자명과 비밀번호를 입력해주세요.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "사용자명 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userOpt.get();
        
        if (!userService.validatePassword(user, password)) {
            response.put("success", false);
            response.put("message", "사용자명 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // JWT 토큰 생성
        String token = jwtService.generateToken(user.getId(), user.getUsername());

        // 쿠키에 토큰 저장 (HttpOnly, Secure, SameSite 설정)
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true); // XSS 공격 방지
        cookie.setSecure(false); // HTTPS에서만 true, 개발 환경에서는 false
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        // SameSite 설정 (Servlet API 6.0+)
        cookie.setAttribute("SameSite", "Lax");
        httpResponse.addCookie(cookie);

        response.put("success", true);
        response.put("message", "로그인 성공");
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail()
        ));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            response.put("success", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        
        // 현재 사용자 정보 확인 (카카오 로그인 사용자인지 확인)
        Long userId = (Long) request.getAttribute("userId");
        boolean isKakaoUser = false;
        
        // 로그아웃 히스토리 저장
        if (userId != null) {
            try {
                // 사용자의 loginType 가져오기
                Optional<User> userOpt = userService.findById(userId);
                String loginType = userOpt.isPresent() && userOpt.get().getLoginType() != null 
                    ? userOpt.get().getLoginType() 
                    : "email";
                System.out.println("[AuthController.logout] 로그아웃 히스토리 저장 시도 - userId: " + userId + ", loginType: " + loginType);
                userLoginHistoryService.saveLogoutHistory(userId, loginType, request);
                System.out.println("[AuthController.logout] 로그아웃 히스토리 저장 성공");
                
                // 카카오 로그인 사용자인지 확인
                isKakaoUser = userService.isKakaoUser(userId);
                if (isKakaoUser) {
                    // 카카오 로그인 사용자인 경우 카카오 로그아웃 URL 생성
                    String kakaoLogoutUrl = kakaoApiService.getKakaoLogoutUrl();
                    if (kakaoLogoutUrl != null) {
                        response.put("kakaoLogoutUrl", kakaoLogoutUrl);
                        response.put("isKakaoUser", true);
                    }
                }
            } catch (Exception e) {
                System.out.println("[AuthController.logout] 로그아웃 히스토리 저장 실패: " + e.getMessage());
                e.printStackTrace();
                // 히스토리 저장 실패해도 로그아웃은 진행
            }
        }
        
        // 쿠키 삭제
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 삭제
        httpResponse.addCookie(cookie);
        
        response.put("success", true);
        response.put("message", "로그아웃되었습니다.");
        if (!isKakaoUser) {
            response.put("isKakaoUser", false);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao/login")
    public ResponseEntity<?> loginWithKakao(@RequestBody Map<String, String> kakaoRequest, HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        
        String accessToken = kakaoRequest.get("accessToken");
        
        if (accessToken == null || accessToken.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "카카오 액세스 토큰이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 카카오 API로 사용자 정보 조회
            // TODO: 카카오 REST API를 호출하여 사용자 정보를 가져오는 로직 구현
            // 현재는 간단한 구현으로, 실제로는 카카오 API를 호출해야 함
            // 예: https://kapi.kakao.com/v2/user/me
            
            response.put("success", false);
            response.put("message", "카카오 로그인 기능은 아직 구현 중입니다.");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카카오 로그인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            response.put("success", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 카카오 소셜 로그인 가입 처리
     * 가입 확인 페이지에서 사용자가 가입을 동의한 경우 호출
     */
    @PostMapping("/kakao/register")
    public ResponseEntity<?> kakaoRegister(@RequestBody Map<String, String> registerRequest,
                                          HttpServletRequest request,
                                          HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        
        String email = registerRequest.get("email");
        String nickname = registerRequest.get("nickname");
        String kakaoId = registerRequest.get("kakaoId");
        String username = registerRequest.get("username");
        
        if (email == null || kakaoId == null || email.trim().isEmpty() || kakaoId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "필수 정보가 누락되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("[Kakao Register] 카카오 소셜 로그인 가입 시작 - email: " + email + ", kakaoId: " + kakaoId + ", username: " + username);
            
            // 사용자 생성
            User user = userService.createKakaoUser(email, nickname, kakaoId, username);
            System.out.println("[Kakao Register] 사용자 생성 완료: userId=" + user.getId() + ", username=" + user.getUsername());
            
            // JWT 토큰 생성
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            
            // 쿠키에 토큰 저장
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            cookie.setAttribute("SameSite", "Lax");
            httpResponse.addCookie(cookie);
            
            // 로그인 히스토리 저장
            try {
                userLoginHistoryService.saveLoginHistory(user.getId(), "kakao", request);
            } catch (Exception e) {
                System.out.println("[Kakao Register] 로그인 히스토리 저장 실패: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "카카오 소셜 로그인 가입이 완료되었습니다.");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("[Kakao Register] 가입 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "가입 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 카카오 로그인 콜백 (인가 코드 받기)
     * 카카오가 redirect_uri로 인가 코드를 포함하여 리다이렉트하는 엔드포인트
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String error,
                                           HttpServletRequest request,
                                           HttpServletResponse httpResponse) {
        System.out.println("=== [Kakao Callback] 시작 ===");
        System.out.println("[Kakao Callback] code: " + code);
        System.out.println("[Kakao Callback] error: " + error);
        
        Map<String, Object> response = new HashMap<>();
        
        // 에러 처리
        if (error != null) {
            System.out.println("[Kakao Callback] 에러 발생: " + error);
            response.put("success", false);
            response.put("message", "카카오 로그인 중 오류가 발생했습니다: " + error);
            return ResponseEntity.badRequest().body(response);
        }
        
        // 인가 코드 확인
        if (code == null || code.trim().isEmpty()) {
            System.out.println("[Kakao Callback] 인가 코드가 없습니다.");
            response.put("success", false);
            response.put("message", "카카오 인가 코드를 받을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("[Kakao Callback] 1. 인가 코드로 액세스 토큰 발급 시작");
            // 1. 인가 코드로 액세스 토큰 발급
            String accessToken = kakaoApiService.getAccessToken(code);
            System.out.println("[Kakao Callback] 1. 액세스 토큰 발급 완료: " + (accessToken != null ? "성공" : "실패"));
            
            System.out.println("[Kakao Callback] 2. 카카오 사용자 정보 조회 시작");
            // 2. 카카오 API로 사용자 정보 조회
            Map<String, Object> kakaoUserInfo = kakaoApiService.getUserInfo(accessToken);
            System.out.println("[Kakao Callback] 2. 사용자 정보 조회 완료");
            
            String email = (String) kakaoUserInfo.get("email");
            String nickname = (String) kakaoUserInfo.get("nickname");
            String kakaoId = (String) kakaoUserInfo.get("kakaoId");
            
            if (email == null || email.trim().isEmpty()) {
                System.out.println("[Kakao Callback] 이메일이 없습니다.");
                response.put("success", false);
                response.put("message", "카카오 이메일 정보를 가져올 수 없습니다. 카카오 계정에서 이메일 제공에 동의해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (kakaoId == null || kakaoId.trim().isEmpty()) {
                System.out.println("[Kakao Callback] 카카오 ID가 없습니다.");
                response.put("success", false);
                response.put("message", "카카오 사용자 ID를 가져올 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("[Kakao Callback] 3. DB에서 사용자 찾기 시작");
            // 3. DB에서 사용자 찾기 (소셜 로그인 테이블 확인)
            User user = userService.findKakaoUser(email, kakaoId);
            
            if (user == null) {
                // 사용자가 없으면 가입 확인 페이지로 리다이렉트
                System.out.println("[Kakao Callback] 신규 사용자 - 가입 확인 페이지로 리다이렉트");
                try {
                    String redirectUrl = "http://192.168.75.80:8088/social-register?email=" + 
                                        java.net.URLEncoder.encode(email, "UTF-8") +
                                        "&nickname=" + java.net.URLEncoder.encode(nickname != null ? nickname : "", "UTF-8") +
                                        "&kakaoId=" + java.net.URLEncoder.encode(kakaoId, "UTF-8") +
                                        "&provider=kakao";
                    httpResponse.sendRedirect(redirectUrl);
                    return null;
                } catch (java.io.IOException e) {
                    System.out.println("[Kakao Callback] 리다이렉트 실패: " + e.getMessage());
                    response.put("success", false);
                    response.put("message", "가입 확인 페이지로 이동하는 중 오류가 발생했습니다.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            
            System.out.println("[Kakao Callback] 3. 기존 사용자 찾음: userId=" + user.getId() + ", username=" + user.getUsername());
            
            System.out.println("[Kakao Callback] 4. JWT 토큰 생성 시작");
            // 4. JWT 토큰 생성
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            System.out.println("[Kakao Callback] 4. JWT 토큰 생성 완료: " + (token != null ? "성공" : "실패"));
            
            System.out.println("[Kakao Callback] 5. 쿠키에 토큰 저장 시작");
            // 5. 쿠키에 토큰 저장
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            cookie.setAttribute("SameSite", "Lax");
            httpResponse.addCookie(cookie);
            System.out.println("[Kakao Callback] 5. 쿠키 설정 완료: auth_token=" + token.substring(0, Math.min(20, token.length())) + "...");
            
            // 카카오 로그인 히스토리 저장
            try {
                String loginType = user.getLoginType() != null ? user.getLoginType() : "kakao";
                System.out.println("[Kakao Callback] 로그인 히스토리 저장 시도 - userId: " + user.getId() + ", loginType: " + loginType);
                userLoginHistoryService.saveLoginHistory(user.getId(), loginType, request);
                System.out.println("[Kakao Callback] 로그인 히스토리 저장 성공");
            } catch (Exception e) {
                System.out.println("[Kakao Callback] 로그인 히스토리 저장 실패: " + e.getMessage());
                e.printStackTrace();
                // 히스토리 저장 실패해도 로그인은 진행
            }
            
            // 6. 프론트엔드로 리다이렉트 (로그인 성공)
            System.out.println("[Kakao Callback] 6. 프론트엔드로 리다이렉트 시작: http://192.168.75.80:8088/?kakaoLogin=success");
            try {
                httpResponse.sendRedirect("http://192.168.75.80:8088/?kakaoLogin=success");
                System.out.println("[Kakao Callback] 6. 리다이렉트 성공");
            } catch (java.io.IOException e) {
                System.out.println("[Kakao Callback] 6. 리다이렉트 실패: " + e.getMessage());
                // 리다이렉트 실패 시 JSON 응답
                response.put("success", true);
                response.put("message", "카카오 로그인 성공");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                ));
                return ResponseEntity.ok(response);
            }
            
            return null;
            
        } catch (Exception e) {
            System.out.println("[Kakao Callback] 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 네이버 로그인 콜백 (인가 코드 받기)
     * 네이버가 redirect_uri로 인가 코드를 포함하여 리다이렉트하는 엔드포인트
     */
    @GetMapping("/naver/callback")
    public ResponseEntity<?> naverCallback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state,
                                           @RequestParam(required = false) String error,
                                           HttpServletRequest request,
                                           HttpServletResponse httpResponse) {
        System.out.println("=== [Naver Callback] 시작 ===");
        System.out.println("[Naver Callback] code: " + code);
        System.out.println("[Naver Callback] state: " + state);
        System.out.println("[Naver Callback] error: " + error);

        Map<String, Object> response = new HashMap<>();

        // 에러 처리
        if (error != null) {
            System.out.println("[Naver Callback] 에러 발생: " + error);
            response.put("success", false);
            response.put("message", "네이버 로그인 중 오류가 발생했습니다: " + error);
            return ResponseEntity.badRequest().body(response);
        }

        // 인가 코드 확인
        if (code == null || code.trim().isEmpty()) {
            System.out.println("[Naver Callback] 인가 코드가 없습니다.");
            response.put("success", false);
            response.put("message", "네이버 인가 코드를 받을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            System.out.println("[Naver Callback] 1. 인가 코드로 액세스 토큰 발급 시작");
            // 1. 인가 코드로 액세스 토큰 발급
            String accessToken = naverApiService.getAccessToken(code);
            System.out.println("[Naver Callback] 1. 액세스 토큰 발급 완료: " + (accessToken != null ? "성공" : "실패"));

            System.out.println("[Naver Callback] 2. 액세스 토큰으로 사용자 정보 조회 시작");
            // 2. 액세스 토큰으로 사용자 정보 조회
            Map<String, Object> naverUserInfo = naverApiService.getUserInfo(accessToken);
            System.out.println("[Naver Callback] 2. 사용자 정보 조회 완료: " + naverUserInfo);

            String email = (String) naverUserInfo.get("email");
            String nickname = (String) naverUserInfo.get("nickname");
            String naverId = (String) naverUserInfo.get("naverId");

            if (email == null || email.trim().isEmpty()) {
                System.out.println("[Naver Callback] 이메일이 없습니다.");
                response.put("success", false);
                response.put("message", "네이버 이메일 정보를 가져올 수 없습니다. 네이버 계정에서 이메일 제공에 동의해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            if (naverId == null || naverId.trim().isEmpty()) {
                System.out.println("[Naver Callback] 네이버 ID가 없습니다.");
                response.put("success", false);
                response.put("message", "네이버 사용자 ID를 가져올 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("[Naver Callback] 3. DB에서 사용자 찾기 시작");
            // 3. DB에서 사용자 찾기 (신규 가입 여부 확인)
            User user = userService.findNaverUser(email, naverId);

            if (user != null) {
                System.out.println("[Naver Callback] 3. 기존 사용자 찾음: userId=" + user.getId() + ", username=" + user.getUsername());
                // 4. JWT 토큰 생성
                String token = jwtService.generateToken(user.getId(), user.getUsername());
                System.out.println("[Naver Callback] 4. JWT 토큰 생성 완료: " + (token != null ? "성공" : "실패"));

                System.out.println("[Naver Callback] 5. 쿠키에 토큰 저장 시작");
                // 5. 쿠키에 토큰 저장
                Cookie cookie = new Cookie("auth_token", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
                cookie.setAttribute("SameSite", "Lax");
                httpResponse.addCookie(cookie);
                System.out.println("[Naver Callback] 5. 쿠키 설정 완료: auth_token=" + token.substring(0, Math.min(20, token.length())) + "...");

                // 네이버 로그인 히스토리 저장
                try {
                    String loginType = user.getLoginType() != null ? user.getLoginType() : "naver";
                    System.out.println("[Naver Callback] 로그인 히스토리 저장 시도 - userId: " + user.getId() + ", loginType: " + loginType);
                    userLoginHistoryService.saveLoginHistory(user.getId(), loginType, request);
                    System.out.println("[Naver Callback] 로그인 히스토리 저장 성공");
                } catch (Exception e) {
                    System.out.println("[Naver Callback] 로그인 히스토리 저장 실패: " + e.getMessage());
                    e.printStackTrace();
                    // 히스토리 저장 실패해도 로그인은 진행
                }

                // 6. 프론트엔드로 리다이렉트 (로그인 성공)
                System.out.println("[Naver Callback] 6. 프론트엔드로 리다이렉트 시작: http://192.168.75.80:8088/?naverLogin=success");
                try {
                    httpResponse.sendRedirect("http://192.168.75.80:8088/?naverLogin=success");
                    System.out.println("[Naver Callback] 6. 리다이렉트 성공");
                } catch (java.io.IOException e) {
                    System.out.println("[Naver Callback] 6. 리다이렉트 실패: " + e.getMessage());
                    // 리다이렉트 실패 시 JSON 응답
                    response.put("success", true);
                    response.put("message", "네이버 로그인 성공");
                    response.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                    ));
                    return ResponseEntity.ok(response);
                }
                
                return null;
            } else {
                System.out.println("[Naver Callback] 3. 신규 사용자 - 가입 확인 페이지로 리다이렉트");
                // 신규 사용자: 가입 확인 페이지로 리다이렉트
                String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
                String encodedNickname = nickname != null ? URLEncoder.encode(nickname, StandardCharsets.UTF_8) : "";
                String encodedNaverId = URLEncoder.encode(naverId, StandardCharsets.UTF_8);
                String redirectUrl = String.format("http://192.168.75.80:8088/social-register?email=%s&nickname=%s&naverId=%s&provider=naver",
                        encodedEmail, encodedNickname, encodedNaverId);
                httpResponse.sendRedirect(redirectUrl);
                System.out.println("[Naver Callback] 신규 사용자 리다이렉트 성공: " + redirectUrl);
            }
            return null; // 리다이렉트 처리했으므로 null 반환
        } catch (Exception e) {
            System.out.println("[Naver Callback] 네이버 로그인 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "네이버 로그인 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 네이버 소셜 로그인 가입 처리
     * 가입 확인 페이지에서 사용자가 가입을 동의한 경우 호출
     */
    @PostMapping("/naver/register")
    public ResponseEntity<?> naverRegister(@RequestBody Map<String, String> registerRequest,
                                          HttpServletRequest request,
                                          HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        
        String email = registerRequest.get("email");
        String nickname = registerRequest.get("nickname");
        String naverId = registerRequest.get("naverId");
        String username = registerRequest.get("username");
        
        if (email == null || naverId == null || email.trim().isEmpty() || naverId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "필수 정보가 누락되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("[Naver Register] 네이버 소셜 로그인 가입 시작 - email: " + email + ", naverId: " + naverId + ", username: " + username);
            
            // 사용자 생성
            User user = userService.createNaverUser(email, nickname, naverId, username);
            System.out.println("[Naver Register] 사용자 생성 완료: userId=" + user.getId() + ", username=" + user.getUsername());
            
            // JWT 토큰 생성
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            
            // 쿠키에 토큰 저장
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            cookie.setAttribute("SameSite", "Lax");
            httpResponse.addCookie(cookie);
            
            // 로그인 히스토리 저장
            try {
                userLoginHistoryService.saveLoginHistory(user.getId(), "naver", request);
            } catch (Exception e) {
                System.out.println("[Naver Register] 로그인 히스토리 저장 실패: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "네이버 소셜 로그인 가입이 완료되었습니다.");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("[Naver Register] 가입 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "네이버 계정으로 회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
