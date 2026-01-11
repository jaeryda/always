package com.always.service;

import com.always.entity.UserLoginHistory;
import com.always.mapper.UserLoginHistoryMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class UserLoginHistoryService {

    @Autowired
    private UserLoginHistoryMapper userLoginHistoryMapper;

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // X-Forwarded-For는 여러 IP가 콤마로 구분될 수 있음
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 로그인 히스토리 저장
     */
    public void saveLoginHistory(Long userId, String loginType, HttpServletRequest request) {
        try {
            System.out.println("[UserLoginHistoryService] 로그인 히스토리 저장 시작 - userId: " + userId + ", loginType: " + loginType);
            UserLoginHistory history = new UserLoginHistory();
            history.setUserId(userId);
            history.setActionType("LOGIN");
            history.setIpAddress(getClientIpAddress(request));
            history.setUserAgent(request.getHeader("User-Agent"));
            history.setLoginType(loginType);
            history.setCreatedAt(LocalDateTime.now());
            userLoginHistoryMapper.insert(history);
            System.out.println("[UserLoginHistoryService] 로그인 히스토리 저장 완료 - id: " + history.getId());
        } catch (Exception e) {
            System.out.println("[UserLoginHistoryService] 로그인 히스토리 저장 실패: " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 다시 throw하여 상위에서 확인 가능하도록
        }
    }

    /**
     * 로그아웃 히스토리 저장
     */
    public void saveLogoutHistory(Long userId, String loginType, HttpServletRequest request) {
        try {
            System.out.println("[UserLoginHistoryService] 로그아웃 히스토리 저장 시작 - userId: " + userId + ", loginType: " + loginType);
            UserLoginHistory history = new UserLoginHistory();
            history.setUserId(userId);
            history.setActionType("LOGOUT");
            history.setIpAddress(getClientIpAddress(request));
            history.setUserAgent(request.getHeader("User-Agent"));
            history.setLoginType(loginType);  // 로그아웃 시에도 loginType 저장
            history.setCreatedAt(LocalDateTime.now());
            userLoginHistoryMapper.insert(history);
            System.out.println("[UserLoginHistoryService] 로그아웃 히스토리 저장 완료 - id: " + history.getId());
        } catch (Exception e) {
            System.out.println("[UserLoginHistoryService] 로그아웃 히스토리 저장 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}

