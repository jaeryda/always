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
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.0.2:8088"})
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
            response.put("message", "?낅젰 ?뺣낫瑜??뺤씤?댁＜?몄슂.");
            response.put("errors", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            User savedUser = userService.register(user);
            response.put("success", true);
            response.put("message", "?뚯썝媛?낆씠 ?꾨즺?섏뿀?듬땲??");
            response.put("user", Map.of(
                "id", savedUser.getId(),
                "username", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "role", savedUser.getRole()
            ));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "?뚯썝媛??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
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
            response.put("message", "?ъ슜?먮챸怨?鍮꾨?踰덊샇瑜??낅젰?댁＜?몄슂.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userService.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "?ъ슜?먮챸 ?먮뒗 鍮꾨?踰덊샇媛 ?щ컮瑜댁? ?딆뒿?덈떎.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        User user = userOpt.get();
        
        if (!userService.validatePassword(user, password)) {
            response.put("success", false);
            response.put("message", "?ъ슜?먮챸 ?먮뒗 鍮꾨?踰덊샇媛 ?щ컮瑜댁? ?딆뒿?덈떎.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // JWT ?좏겙 ?앹꽦
        String token = jwtService.generateToken(user.getId(), user.getUsername());

        // 荑좏궎???좏겙 ???(HttpOnly, Secure, SameSite ?ㅼ젙)
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true); // XSS 怨듦꺽 諛⑹?
        cookie.setSecure(false); // HTTPS?먯꽌留?true, 媛쒕컻 ?섍꼍?먯꽌??false
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7??
        // SameSite ?ㅼ젙 (Servlet API 6.0+)
        cookie.setAttribute("SameSite", "Lax");
        httpResponse.addCookie(cookie);

        response.put("success", true);
        response.put("message", "濡쒓렇???깃났");
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
                "role", user.getRole()
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
                response.put("message", "?몄쬆 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "?ъ슜?먮? 李얠쓣 ???놁뒿?덈떎.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            response.put("success", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "?ъ슜???뺣낫 議고쉶 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        
        // ?꾩옱 ?ъ슜???뺣낫 ?뺤씤 (移댁뭅??濡쒓렇???ъ슜?먯씤吏 ?뺤씤)
        Long userId = (Long) request.getAttribute("userId");
        boolean isKakaoUser = false;
        
        // 濡쒓렇?꾩썐 ?덉뒪?좊━ ???
        if (userId != null) {
            try {
                // ?ъ슜?먯쓽 loginType 媛?몄삤湲?
                Optional<User> userOpt = userService.findById(userId);
                String loginType = userOpt.isPresent() && userOpt.get().getLoginType() != null 
                    ? userOpt.get().getLoginType() 
                    : "email";
                System.out.println("[AuthController.logout] 濡쒓렇?꾩썐 ?덉뒪?좊━ ????쒕룄 - userId: " + userId + ", loginType: " + loginType);
                userLoginHistoryService.saveLogoutHistory(userId, loginType, request);
                System.out.println("[AuthController.logout] 濡쒓렇?꾩썐 ?덉뒪?좊━ ????깃났");
                
                // 移댁뭅??濡쒓렇???ъ슜?먯씤吏 ?뺤씤
                isKakaoUser = userService.isKakaoUser(userId);
                if (isKakaoUser) {
                    // 移댁뭅??濡쒓렇???ъ슜?먯씤 寃쎌슦 移댁뭅??濡쒓렇?꾩썐 URL ?앹꽦
                    String kakaoLogoutUrl = kakaoApiService.getKakaoLogoutUrl();
                    if (kakaoLogoutUrl != null) {
                        response.put("kakaoLogoutUrl", kakaoLogoutUrl);
                        response.put("isKakaoUser", true);
                    }
                }
            } catch (Exception e) {
                System.out.println("[AuthController.logout] 濡쒓렇?꾩썐 ?덉뒪?좊━ ????ㅽ뙣: " + e.getMessage());
                e.printStackTrace();
                // ?덉뒪?좊━ ????ㅽ뙣?대룄 濡쒓렇?꾩썐? 吏꾪뻾
            }
        }
        
        // 荑좏궎 ??젣
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 荑좏궎 ??젣
        httpResponse.addCookie(cookie);
        
        response.put("success", true);
        response.put("message", "濡쒓렇?꾩썐?섏뿀?듬땲??");
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
            response.put("message", "移댁뭅???≪꽭???좏겙???꾩슂?⑸땲??");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 移댁뭅??API濡??ъ슜???뺣낫 議고쉶
            // TODO: 移댁뭅??REST API瑜??몄텧?섏뿬 ?ъ슜???뺣낫瑜?媛?몄삤??濡쒖쭅 援ы쁽
            // ?꾩옱??媛꾨떒??援ы쁽?쇰줈, ?ㅼ젣濡쒕뒗 移댁뭅??API瑜??몄텧?댁빞 ??
            // ?? https://kapi.kakao.com/v2/user/me
            
            response.put("success", false);
            response.put("message", "移댁뭅??濡쒓렇??湲곕뒫? ?꾩쭅 援ы쁽 以묒엯?덈떎.");
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "移댁뭅??濡쒓렇??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getMessage());
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
                response.put("message", "?ъ슜?먮? 李얠쓣 ???놁뒿?덈떎.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOpt.get();
            response.put("success", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "?ъ슜???뺣낫 議고쉶 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 移댁뭅???뚯뀥 濡쒓렇??媛??泥섎━
     * 媛???뺤씤 ?섏씠吏?먯꽌 ?ъ슜?먭? 媛?낆쓣 ?숈쓽??寃쎌슦 ?몄텧
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
            response.put("message", "?꾩닔 ?뺣낫媛 ?꾨씫?섏뿀?듬땲??");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("[Kakao Register] 移댁뭅???뚯뀥 濡쒓렇??媛???쒖옉 - email: " + email + ", kakaoId: " + kakaoId + ", username: " + username);
            
            // ?ъ슜???앹꽦
            User user = userService.createKakaoUser(email, nickname, kakaoId, username);
            System.out.println("[Kakao Register] ?ъ슜???앹꽦 ?꾨즺: userId=" + user.getId() + ", username=" + user.getUsername());
            
            // JWT ?좏겙 ?앹꽦
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            
            // 荑좏궎???좏겙 ???
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7??
            cookie.setAttribute("SameSite", "Lax");
            httpResponse.addCookie(cookie);
            
            // 濡쒓렇???덉뒪?좊━ ???
            try {
                userLoginHistoryService.saveLoginHistory(user.getId(), "kakao", request);
            } catch (Exception e) {
                System.out.println("[Kakao Register] 濡쒓렇???덉뒪?좊━ ????ㅽ뙣: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "移댁뭅???뚯뀥 濡쒓렇??媛?낆씠 ?꾨즺?섏뿀?듬땲??");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("[Kakao Register] 媛??泥섎━ 以??ㅻ쪟: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "媛??泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 移댁뭅??濡쒓렇??肄쒕갚 (?멸? 肄붾뱶 諛쏄린)
     * 移댁뭅?ㅺ? redirect_uri濡??멸? 肄붾뱶瑜??ы븿?섏뿬 由щ떎?대젆?명븯???붾뱶?ъ씤??
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String error,
                                           HttpServletRequest request,
                                           HttpServletResponse httpResponse) {
        System.out.println("=== [Kakao Callback] ?쒖옉 ===");
        System.out.println("[Kakao Callback] code: " + code);
        System.out.println("[Kakao Callback] error: " + error);
        
        Map<String, Object> response = new HashMap<>();
        
        // ?먮윭 泥섎━
        if (error != null) {
            System.out.println("[Kakao Callback] ?먮윭 諛쒖깮: " + error);
            response.put("success", false);
            response.put("message", "移댁뭅??濡쒓렇??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + error);
            return ResponseEntity.badRequest().body(response);
        }
        
        // ?멸? 肄붾뱶 ?뺤씤
        if (code == null || code.trim().isEmpty()) {
            System.out.println("[Kakao Callback] ?멸? 肄붾뱶媛 ?놁뒿?덈떎.");
            response.put("success", false);
            response.put("message", "移댁뭅???멸? 肄붾뱶瑜?諛쏆쓣 ???놁뒿?덈떎.");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("[Kakao Callback] 1. ?멸? 肄붾뱶濡??≪꽭???좏겙 諛쒓툒 ?쒖옉");
            // 1. ?멸? 肄붾뱶濡??≪꽭???좏겙 諛쒓툒
            String accessToken = kakaoApiService.getAccessToken(code);
            System.out.println("[Kakao Callback] 1. ?≪꽭???좏겙 諛쒓툒 ?꾨즺: " + (accessToken != null ? "?깃났" : "?ㅽ뙣"));
            
            System.out.println("[Kakao Callback] 2. 移댁뭅???ъ슜???뺣낫 議고쉶 ?쒖옉");
            // 2. 移댁뭅??API濡??ъ슜???뺣낫 議고쉶
            Map<String, Object> kakaoUserInfo = kakaoApiService.getUserInfo(accessToken);
            System.out.println("[Kakao Callback] 2. ?ъ슜???뺣낫 議고쉶 ?꾨즺");
            
            String email = (String) kakaoUserInfo.get("email");
            String nickname = (String) kakaoUserInfo.get("nickname");
            String kakaoId = (String) kakaoUserInfo.get("kakaoId");
            
            if (email == null || email.trim().isEmpty()) {
                System.out.println("[Kakao Callback] ?대찓?쇱씠 ?놁뒿?덈떎.");
                response.put("success", false);
                response.put("message", "移댁뭅???대찓???뺣낫瑜?媛?몄삱 ???놁뒿?덈떎. 移댁뭅??怨꾩젙?먯꽌 ?대찓???쒓났???숈쓽?댁＜?몄슂.");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (kakaoId == null || kakaoId.trim().isEmpty()) {
                System.out.println("[Kakao Callback] 移댁뭅??ID媛 ?놁뒿?덈떎.");
                response.put("success", false);
                response.put("message", "移댁뭅???ъ슜??ID瑜?媛?몄삱 ???놁뒿?덈떎.");
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("[Kakao Callback] 3. DB?먯꽌 ?ъ슜??李얘린 ?쒖옉");
            // 3. DB?먯꽌 ?ъ슜??李얘린 (?뚯뀥 濡쒓렇???뚯씠釉??뺤씤)
            User user = userService.findKakaoUser(email, kakaoId);
            
            if (user == null) {
                // ?ъ슜?먭? ?놁쑝硫?媛???뺤씤 ?섏씠吏濡?由щ떎?대젆??
                System.out.println("[Kakao Callback] 신규 사용자 - 가입확인 페이지로 리다이렉트");
                try {
                    String redirectUrl = "http://192.168.0.2:8088/social-register?email=" + 
                                        java.net.URLEncoder.encode(email, "UTF-8") +
                                        "&nickname=" + java.net.URLEncoder.encode(nickname != null ? nickname : "", "UTF-8") +
                                        "&kakaoId=" + java.net.URLEncoder.encode(kakaoId, "UTF-8") +
                                        "&provider=kakao";
                    httpResponse.sendRedirect(redirectUrl);
                    return null;
                } catch (java.io.IOException e) {
                    System.out.println("[Kakao Callback] 由щ떎?대젆???ㅽ뙣: " + e.getMessage());
                    response.put("success", false);
                    response.put("message", "媛???뺤씤 ?섏씠吏濡??대룞?섎뒗 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }
            
            System.out.println("[Kakao Callback] 3. 湲곗〈 ?ъ슜??李얠쓬: userId=" + user.getId() + ", username=" + user.getUsername());
            
            System.out.println("[Kakao Callback] 4. JWT ?좏겙 ?앹꽦 ?쒖옉");
            // 4. JWT ?좏겙 ?앹꽦
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            System.out.println("[Kakao Callback] 4. JWT ?좏겙 ?앹꽦 ?꾨즺: " + (token != null ? "?깃났" : "?ㅽ뙣"));
            
            System.out.println("[Kakao Callback] 5. 荑좏궎???좏겙 ????쒖옉");
            // 5. 荑좏궎???좏겙 ???
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7??
            cookie.setAttribute("SameSite", "Lax");
            httpResponse.addCookie(cookie);
            System.out.println("[Kakao Callback] 5. 荑좏궎 ?ㅼ젙 ?꾨즺: auth_token=" + token.substring(0, Math.min(20, token.length())) + "...");
            
            // 移댁뭅??濡쒓렇???덉뒪?좊━ ???
            try {
                String loginType = user.getLoginType() != null ? user.getLoginType() : "kakao";
                System.out.println("[Kakao Callback] 濡쒓렇???덉뒪?좊━ ????쒕룄 - userId: " + user.getId() + ", loginType: " + loginType);
                userLoginHistoryService.saveLoginHistory(user.getId(), loginType, request);
                System.out.println("[Kakao Callback] 濡쒓렇???덉뒪?좊━ ????깃났");
            } catch (Exception e) {
                System.out.println("[Kakao Callback] 濡쒓렇???덉뒪?좊━ ????ㅽ뙣: " + e.getMessage());
                e.printStackTrace();
                // ?덉뒪?좊━ ????ㅽ뙣?대룄 濡쒓렇?몄? 吏꾪뻾
            }
            
            // 6. ?꾨줎?몄뿏?쒕줈 由щ떎?대젆??(濡쒓렇???깃났)
            System.out.println("[Kakao Callback] 6. ?꾨줎?몄뿏?쒕줈 由щ떎?대젆???쒖옉: http://192.168.0.2:8088/?kakaoLogin=success");
            try {
                httpResponse.sendRedirect("http://192.168.0.2:8088/?kakaoLogin=success");
                System.out.println("[Kakao Callback] 6. 由щ떎?대젆???깃났");
            } catch (java.io.IOException e) {
                System.out.println("[Kakao Callback] 6. 由щ떎?대젆???ㅽ뙣: " + e.getMessage());
                // 由щ떎?대젆???ㅽ뙣 ??JSON ?묐떟
                response.put("success", true);
                response.put("message", "移댁뭅??濡쒓렇???깃났");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                "role", user.getRole()
                ));
                return ResponseEntity.ok(response);
            }
            
            return null;
            
        } catch (Exception e) {
            System.out.println("[Kakao Callback] 泥섎━ 以??ㅻ쪟: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "移댁뭅??濡쒓렇??泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ?ㅼ씠踰?濡쒓렇??肄쒕갚 (?멸? 肄붾뱶 諛쏄린)
     * ?ㅼ씠踰꾧? redirect_uri濡??멸? 肄붾뱶瑜??ы븿?섏뿬 由щ떎?대젆?명븯???붾뱶?ъ씤??
     */
    @GetMapping("/naver/callback")
    public ResponseEntity<?> naverCallback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state,
                                           @RequestParam(required = false) String error,
                                           HttpServletRequest request,
                                           HttpServletResponse httpResponse) {
        System.out.println("=== [Naver Callback] ?쒖옉 ===");
        System.out.println("[Naver Callback] code: " + code);
        System.out.println("[Naver Callback] state: " + state);
        System.out.println("[Naver Callback] error: " + error);

        Map<String, Object> response = new HashMap<>();

        // ?먮윭 泥섎━
        if (error != null) {
            System.out.println("[Naver Callback] ?먮윭 諛쒖깮: " + error);
            response.put("success", false);
            response.put("message", "?ㅼ씠踰?濡쒓렇??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + error);
            return ResponseEntity.badRequest().body(response);
        }

        // ?멸? 肄붾뱶 ?뺤씤
        if (code == null || code.trim().isEmpty()) {
            System.out.println("[Naver Callback] ?멸? 肄붾뱶媛 ?놁뒿?덈떎.");
            response.put("success", false);
            response.put("message", "?ㅼ씠踰??멸? 肄붾뱶瑜?諛쏆쓣 ???놁뒿?덈떎.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            System.out.println("[Naver Callback] 1. ?멸? 肄붾뱶濡??≪꽭???좏겙 諛쒓툒 ?쒖옉");
            // 1. ?멸? 肄붾뱶濡??≪꽭???좏겙 諛쒓툒
            String accessToken = naverApiService.getAccessToken(code);
            System.out.println("[Naver Callback] 1. ?≪꽭???좏겙 諛쒓툒 ?꾨즺: " + (accessToken != null ? "?깃났" : "?ㅽ뙣"));

            System.out.println("[Naver Callback] 2. ?≪꽭???좏겙?쇰줈 ?ъ슜???뺣낫 議고쉶 ?쒖옉");
            // 2. ?≪꽭???좏겙?쇰줈 ?ъ슜???뺣낫 議고쉶
            Map<String, Object> naverUserInfo = naverApiService.getUserInfo(accessToken);
            System.out.println("[Naver Callback] 2. ?ъ슜???뺣낫 議고쉶 ?꾨즺: " + naverUserInfo);

            String email = (String) naverUserInfo.get("email");
            String nickname = (String) naverUserInfo.get("nickname");
            String naverId = (String) naverUserInfo.get("naverId");

            if (email == null || email.trim().isEmpty()) {
                System.out.println("[Naver Callback] ?대찓?쇱씠 ?놁뒿?덈떎.");
                response.put("success", false);
                response.put("message", "?ㅼ씠踰??대찓???뺣낫瑜?媛?몄삱 ???놁뒿?덈떎. ?ㅼ씠踰?怨꾩젙?먯꽌 ?대찓???쒓났???숈쓽?댁＜?몄슂.");
                return ResponseEntity.badRequest().body(response);
            }

            if (naverId == null || naverId.trim().isEmpty()) {
                System.out.println("[Naver Callback] ?ㅼ씠踰?ID媛 ?놁뒿?덈떎.");
                response.put("success", false);
                response.put("message", "?ㅼ씠踰??ъ슜??ID瑜?媛?몄삱 ???놁뒿?덈떎.");
                return ResponseEntity.badRequest().body(response);
            }

            System.out.println("[Naver Callback] 3. DB?먯꽌 ?ъ슜??李얘린 ?쒖옉");
            // 3. DB?먯꽌 ?ъ슜??李얘린 (?좉퇋 媛???щ? ?뺤씤)
            User user = userService.findNaverUser(email, naverId);

            if (user != null) {
                System.out.println("[Naver Callback] 3. 湲곗〈 ?ъ슜??李얠쓬: userId=" + user.getId() + ", username=" + user.getUsername());
                // 4. JWT ?좏겙 ?앹꽦
                String token = jwtService.generateToken(user.getId(), user.getUsername());
                System.out.println("[Naver Callback] 4. JWT ?좏겙 ?앹꽦 ?꾨즺: " + (token != null ? "?깃났" : "?ㅽ뙣"));

                System.out.println("[Naver Callback] 5. 荑좏궎???좏겙 ????쒖옉");
                // 5. 荑좏궎???좏겙 ???
                Cookie cookie = new Cookie("auth_token", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7??
                cookie.setAttribute("SameSite", "Lax");
                httpResponse.addCookie(cookie);
                System.out.println("[Naver Callback] 5. 荑좏궎 ?ㅼ젙 ?꾨즺: auth_token=" + token.substring(0, Math.min(20, token.length())) + "...");

                // ?ㅼ씠踰?濡쒓렇???덉뒪?좊━ ???
                try {
                    String loginType = user.getLoginType() != null ? user.getLoginType() : "naver";
                    System.out.println("[Naver Callback] 濡쒓렇???덉뒪?좊━ ????쒕룄 - userId: " + user.getId() + ", loginType: " + loginType);
                    userLoginHistoryService.saveLoginHistory(user.getId(), loginType, request);
                    System.out.println("[Naver Callback] 濡쒓렇???덉뒪?좊━ ????깃났");
                } catch (Exception e) {
                    System.out.println("[Naver Callback] 濡쒓렇???덉뒪?좊━ ????ㅽ뙣: " + e.getMessage());
                    e.printStackTrace();
                    // ?덉뒪?좊━ ????ㅽ뙣?대룄 濡쒓렇?몄? 吏꾪뻾
                }

                // 6. ?꾨줎?몄뿏?쒕줈 由щ떎?대젆??(濡쒓렇???깃났)
                System.out.println("[Naver Callback] 6. ?꾨줎?몄뿏?쒕줈 由щ떎?대젆???쒖옉: http://192.168.0.2:8088/?naverLogin=success");
                try {
                    httpResponse.sendRedirect("http://192.168.0.2:8088/?naverLogin=success");
                    System.out.println("[Naver Callback] 6. 由щ떎?대젆???깃났");
                } catch (java.io.IOException e) {
                    System.out.println("[Naver Callback] 6. 由щ떎?대젆???ㅽ뙣: " + e.getMessage());
                    // 由щ떎?대젆???ㅽ뙣 ??JSON ?묐떟
                    response.put("success", true);
                    response.put("message", "?ㅼ씠踰?濡쒓렇???깃났");
                    response.put("user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                "role", user.getRole()
                    ));
                    return ResponseEntity.ok(response);
                }
                
                return null;
            } else {
                System.out.println("[Naver Callback] 3. 신규 사용자 - 가입확인 페이지로 리다이렉트");
                // ?좉퇋 ?ъ슜?? 媛???뺤씤 ?섏씠吏濡?由щ떎?대젆??
                String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
                String encodedNickname = nickname != null ? URLEncoder.encode(nickname, StandardCharsets.UTF_8) : "";
                String encodedNaverId = URLEncoder.encode(naverId, StandardCharsets.UTF_8);
                String redirectUrl = String.format("http://192.168.0.2:8088/social-register?email=%s&nickname=%s&naverId=%s&provider=naver",
                        encodedEmail, encodedNickname, encodedNaverId);
                httpResponse.sendRedirect(redirectUrl);
                System.out.println("[Naver Callback] ?좉퇋 ?ъ슜??由щ떎?대젆???깃났: " + redirectUrl);
            }
            return null; // 由щ떎?대젆??泥섎━?덉쑝誘濡?null 諛섑솚
        } catch (Exception e) {
            System.out.println("[Naver Callback] ?ㅼ씠踰?濡쒓렇??以??ㅻ쪟 諛쒖깮: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "?ㅼ씠踰?濡쒓렇??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ?ㅼ씠踰??뚯뀥 濡쒓렇??媛??泥섎━
     * 媛???뺤씤 ?섏씠吏?먯꽌 ?ъ슜?먭? 媛?낆쓣 ?숈쓽??寃쎌슦 ?몄텧
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
            response.put("message", "?꾩닔 ?뺣낫媛 ?꾨씫?섏뿀?듬땲??");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            System.out.println("[Naver Register] ?ㅼ씠踰??뚯뀥 濡쒓렇??媛???쒖옉 - email: " + email + ", naverId: " + naverId + ", username: " + username);
            
            // ?ъ슜???앹꽦
            User user = userService.createNaverUser(email, nickname, naverId, username);
            System.out.println("[Naver Register] ?ъ슜???앹꽦 ?꾨즺: userId=" + user.getId() + ", username=" + user.getUsername());
            
            // JWT ?좏겙 ?앹꽦
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            
            // 荑좏궎???좏겙 ???
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7??
            cookie.setAttribute("SameSite", "Lax");
            httpResponse.addCookie(cookie);
            
            // 濡쒓렇???덉뒪?좊━ ???
            try {
                userLoginHistoryService.saveLoginHistory(user.getId(), "naver", request);
            } catch (Exception e) {
                System.out.println("[Naver Register] 濡쒓렇???덉뒪?좊━ ????ㅽ뙣: " + e.getMessage());
            }
            
            response.put("success", true);
            response.put("message", "?ㅼ씠踰??뚯뀥 濡쒓렇??媛?낆씠 ?꾨즺?섏뿀?듬땲??");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.out.println("[Naver Register] 媛??泥섎━ 以??ㅻ쪟: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "?ㅼ씠踰?怨꾩젙?쇰줈 ?뚯썝媛??以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


