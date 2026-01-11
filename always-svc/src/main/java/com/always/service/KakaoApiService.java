package com.always.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoApiService {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiService.class);
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kakao.rest.api.key}")
    private String restApiKey;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    @Value("${kakao.logout.redirect.uri:http://192.168.75.80:8088/login}")
    private String logoutRedirectUri;

    @Value("${kakao.client.secret:}")
    private String clientSecret;

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     * 
     * @param accessToken 카카오 액세스 토큰
     * @return 사용자 정보 (email, nickname, kakaoId 등)
     * @throws Exception API 호출 실패 시
     */
    public Map<String, Object> getUserInfo(String accessToken) throws Exception {
        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            URL url = new URL(KAKAO_USER_INFO_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // POST 메서드 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            
            int responseCode = conn.getResponseCode();
            log.info("[KakaoApiService.getUserInfo] responseCode: {}", responseCode);
            
            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                String errorResponse = readResponse(br);
                log.error("[KakaoApiService.getUserInfo] Error response: {}", errorResponse);
                throw new RuntimeException("카카오 사용자 정보 조회 실패: HTTP " + responseCode);
            }
            
            String responseBody = readResponse(br);
            log.info("[KakaoApiService.getUserInfo] Response body: {}", responseBody);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 카카오 ID (String으로 변환)
            Long kakaoIdLong = jsonNode.get("id").asLong();
            userInfo.put("kakaoId", String.valueOf(kakaoIdLong));
            
            // 카카오 계정 정보
            JsonNode kakaoAccount = jsonNode.get("kakao_account");
            if (kakaoAccount != null) {
                // 이메일
                if (kakaoAccount.has("email") && kakaoAccount.get("email") != null) {
                    String email = kakaoAccount.get("email").asText();
                    userInfo.put("email", email);
                }
                
                // 닉네임
                if (kakaoAccount.has("profile") && kakaoAccount.get("profile") != null) {
                    JsonNode profile = kakaoAccount.get("profile");
                    if (profile.has("nickname") && profile.get("nickname") != null) {
                        String nickname = profile.get("nickname").asText();
                        userInfo.put("nickname", nickname);
                    }
                }
            }
            
            log.info("[KakaoApiService.getUserInfo] Parsed userInfo: {}", userInfo);
            return userInfo;
            
        } catch (Exception e) {
            log.error("[KakaoApiService.getUserInfo] Exception occurred", e);
            throw e;
        }
    }
    
    /**
     * 인가 코드로 카카오 액세스 토큰 발급
     * 
     * @param code 카카오 인가 코드
     * @return 액세스 토큰
     * @throws Exception API 호출 실패 시
     */
    public String getAccessToken(String code) throws Exception {
        System.out.println("=== [KakaoApiService.getAccessToken] 시작 ===");
        System.out.println("[KakaoApiService.getAccessToken] code: " + code);
        System.out.println("[KakaoApiService.getAccessToken] restApiKey: " + restApiKey);
        System.out.println("[KakaoApiService.getAccessToken] redirectUri: " + redirectUri);
        
        try {
            URL url = new URL(KAKAO_TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // POST 메서드 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true);
            
            // 요청 파라미터 설정 (client_id는 인코딩하지 않음)
            String parameters = "grant_type=authorization_code" +
                    "&client_id=" + restApiKey +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
            
            // Client Secret이 설정되어 있으면 파라미터에 추가
            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                parameters += "&client_secret=" + clientSecret;
                System.out.println("[KakaoApiService.getAccessToken] client_secret 사용");
            } else {
                System.out.println("[KakaoApiService.getAccessToken] client_secret 미사용");
            }
            
            System.out.println("[KakaoApiService.getAccessToken] 요청 파라미터: " + parameters.replace(restApiKey, "***"));
            
            // 요청 본문 전송
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(parameters);
                wr.flush();
            }
            
            int responseCode = conn.getResponseCode();
            System.out.println("[KakaoApiService.getAccessToken] responseCode: " + responseCode);
            log.info("[KakaoApiService.getAccessToken] responseCode: {}", responseCode);
            
            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                String errorResponse = readResponse(br);
                System.out.println("[KakaoApiService.getAccessToken] Error response: " + errorResponse);
                log.error("[KakaoApiService.getAccessToken] Error response: {}", errorResponse);
                throw new RuntimeException("카카오 액세스 토큰 발급 실패: HTTP " + responseCode);
            }
            
            String responseBody = readResponse(br);
            System.out.println("[KakaoApiService.getAccessToken] Response body: " + responseBody);
            log.info("[KakaoApiService.getAccessToken] Response body: {}", responseBody);
            
            // JSON 파싱하여 access_token 추출
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.get("access_token").asText();
            
            System.out.println("[KakaoApiService.getAccessToken] Access token received: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
            log.info("[KakaoApiService.getAccessToken] Access token received");
            System.out.println("=== [KakaoApiService.getAccessToken] 완료 ===");
            return accessToken;
            
        } catch (Exception e) {
            System.out.println("[KakaoApiService.getAccessToken] Exception: " + e.getMessage());
            e.printStackTrace();
            log.error("[KakaoApiService.getAccessToken] Exception occurred", e);
            throw e;
        }
    }
    
    /**
     * 카카오 로그아웃 URL 생성
     * 카카오계정과 함께 로그아웃하기 위한 URL 생성
     * 
     * @return 카카오 로그아웃 URL
     */
    public String getKakaoLogoutUrl() {
        try {
            String encodedRedirectUri = URLEncoder.encode(logoutRedirectUri, StandardCharsets.UTF_8);
            System.out.println("[KakaoApiService.getKakaoLogoutUrl] logoutRedirectUri: " + logoutRedirectUri);
            return "https://kauth.kakao.com/oauth/logout?client_id=" + restApiKey + "&logout_redirect_uri=" + encodedRedirectUri;
        } catch (Exception e) {
            log.error("[KakaoApiService.getKakaoLogoutUrl] URL 생성 실패", e);
            return null;
        }
    }
    
    private String readResponse(BufferedReader br) throws Exception {
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        return response.toString();
    }
}

