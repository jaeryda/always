-- 사용자 로그인/로그아웃 히스토리 테이블 생성 스크립트
-- MySQL에 접속한 후 이 스크립트를 실행하세요

USE always_db;

-- 사용자 로그인/로그아웃 히스토리 테이블 생성
CREATE TABLE users_H (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '사용자 ID (users 테이블 참조)',
    action_type VARCHAR(20) NOT NULL COMMENT '액션 타입 (LOGIN, LOGOUT)',
    ip_address VARCHAR(45) COMMENT 'IP 주소 (IPv6 지원)',
    user_agent TEXT COMMENT 'User Agent 정보',
    login_type VARCHAR(50) COMMENT '로그인 타입 (email, kakao, naver, google 등)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '발생 시각',
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 로그인/로그아웃 히스토리 테이블';




