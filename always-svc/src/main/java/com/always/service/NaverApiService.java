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
public class NaverApiService {

    private static final Logger log = LoggerFactory.getLogger(NaverApiService.class);
    private static final String NAVER_TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String NAVER_USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect.uri}")
    private String redirectUri;

    /**
     * 네이버 액세스 토큰으로 사용자 정보 조회
     * 
     * @param accessToken 네이버 액세스 토큰
     * @return 사용자 정보 (email, nickname, naverId 등)
     * @throws Exception API 호출 실패 시
     */
    public Map<String, Object> getUserInfo(String accessToken) throws Exception {
        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            URL url = new URL(NAVER_USER_INFO_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // GET 메서드 설정
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            int responseCode = conn.getResponseCode();
            log.info("[NaverApiService.getUserInfo] responseCode: {}", responseCode);
            
            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                String errorResponse = readResponse(br);
                log.error("[NaverApiService.getUserInfo] Error response: {}", errorResponse);
                throw new RuntimeException("네이버 사용자 정보 조회 실패: HTTP " + responseCode);
            }
            
            String responseBody = readResponse(br);
            log.info("[NaverApiService.getUserInfo] Response body: {}", responseBody);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 네이버 응답 구조: { "resultcode": "00", "message": "success", "response": { "id": "...", "email": "...", ... } }
            if (!"00".equals(jsonNode.get("resultcode").asText())) {
                throw new RuntimeException("네이버 사용자 정보 조회 실패: " + jsonNode.get("message").asText());
            }
            
            JsonNode response = jsonNode.get("response");
            if (response != null) {
                // 네이버 ID
                String naverId = response.get("id").asText();
                userInfo.put("naverId", naverId);
                
                // 이메일
                if (response.has("email") && response.get("email") != null) {
                    String email = response.get("email").asText();
                    userInfo.put("email", email);
                }
                
                // 닉네임
                if (response.has("nickname") && response.get("nickname") != null) {
                    String nickname = response.get("nickname").asText();
                    userInfo.put("nickname", nickname);
                }
                
                // 이름
                if (response.has("name") && response.get("name") != null) {
                    String name = response.get("name").asText();
                    userInfo.put("name", name);
                }
            }
            
            log.info("[NaverApiService.getUserInfo] Parsed userInfo: {}", userInfo);
            return userInfo;
            
        } catch (Exception e) {
            log.error("[NaverApiService.getUserInfo] Exception occurred", e);
            throw e;
        }
    }
    
    /**
     * 인가 코드로 네이버 액세스 토큰 발급
     * 
     * @param code 네이버 인가 코드
     * @return 액세스 토큰
     * @throws Exception API 호출 실패 시
     */
    public String getAccessToken(String code) throws Exception {
        System.out.println("=== [NaverApiService.getAccessToken] 시작 ===");
        System.out.println("[NaverApiService.getAccessToken] code: " + code);
        System.out.println("[NaverApiService.getAccessToken] clientId: " + clientId);
        System.out.println("[NaverApiService.getAccessToken] redirectUri: " + redirectUri);
        
        try {
            URL url = new URL(NAVER_TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // POST 메서드 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setDoOutput(true);
            
            // 요청 파라미터 생성
            String parameters = "grant_type=authorization_code" +
                    "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                    "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                    "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
            
            System.out.println("[NaverApiService.getAccessToken] 요청 파라미터: " + parameters.replace(clientId, "***").replace(clientSecret, "***"));
            
            // 요청 본문 전송
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(parameters);
                wr.flush();
            }
            
            int responseCode = conn.getResponseCode();
            System.out.println("[NaverApiService.getAccessToken] responseCode: " + responseCode);
            log.info("[NaverApiService.getAccessToken] responseCode: {}", responseCode);
            
            BufferedReader br;
            if (responseCode >= 200 && responseCode <= 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                String errorResponse = readResponse(br);
                System.out.println("[NaverApiService.getAccessToken] Error response: " + errorResponse);
                log.error("[NaverApiService.getAccessToken] Error response: {}", errorResponse);
                throw new RuntimeException("네이버 액세스 토큰 발급 실패: HTTP " + responseCode);
            }
            
            String responseBody = readResponse(br);
            System.out.println("[NaverApiService.getAccessToken] Response body: " + responseBody);
            log.info("[NaverApiService.getAccessToken] Response body: {}", responseBody);
            
            // JSON 파싱하여 access_token 추출
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.get("access_token").asText();
            
            System.out.println("[NaverApiService.getAccessToken] Access token received: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
            log.info("[NaverApiService.getAccessToken] Access token received");
            System.out.println("=== [NaverApiService.getAccessToken] 완료 ===");
            return accessToken;
            
        } catch (Exception e) {
            System.out.println("[NaverApiService.getAccessToken] Exception: " + e.getMessage());
            e.printStackTrace();
            log.error("[NaverApiService.getAccessToken] Exception occurred", e);
            throw e;
        }
    }
    
    /**
     * HTTP 응답 읽기
     */
    private String readResponse(BufferedReader reader) throws Exception {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        return response.toString();
    }
}

