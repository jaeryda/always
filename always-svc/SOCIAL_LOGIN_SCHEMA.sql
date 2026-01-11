-- 소셜 로그인 테이블 생성 스크립트
-- MySQL에 접속한 후 이 스크립트를 실행하세요

USE always_db;

-- users 테이블에 login_type 컬럼 추가 (로그인 타입: email, kakao, naver, google 등)
-- IF NOT EXISTS는 MySQL 5.7.4+에서만 지원되므로, 컬럼이 이미 있는지 확인 후 추가
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'login_type';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(50) DEFAULT ''email'' COMMENT ''로그인 타입 (email, kakao, naver, google 등)''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 인덱스 추가 (이미 있으면 에러가 나지만 무시 가능)
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (index_name = 'idx_login_type')
  ) > 0,
  'SELECT 1',
  CONCAT('CREATE INDEX idx_login_type ON ', @tablename, ' (login_type)')
));
PREPARE createIndexIfNotExists FROM @preparedStatement;
EXECUTE createIndexIfNotExists;
DEALLOCATE PREPARE createIndexIfNotExists;

-- 기존 데이터는 'email'로 설정 (일반 로그인)
UPDATE users SET login_type = 'email' WHERE login_type IS NULL OR login_type = '';

-- 소셜 로그인 테이블 생성
CREATE TABLE social_logins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL COMMENT '소셜 로그인 제공자 (kakao, google, naver 등)',
    provider_user_id VARCHAR(255) NOT NULL COMMENT '소셜 로그인 제공자의 사용자 ID',
    email VARCHAR(255) NOT NULL COMMENT '이메일 (users 테이블과 연결)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_provider_user_id (provider, provider_user_id),
    INDEX idx_email (email),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='소셜 로그인 정보 테이블';

