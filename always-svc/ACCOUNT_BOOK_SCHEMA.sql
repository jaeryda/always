-- 가계부 데이터베이스 스키마
-- MySQL 데이터베이스에 실행

USE always_db;

-- 카테고리 테이블
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '카테고리명 (예: 식비, 교통비, 쇼핑)',
    type VARCHAR(10) NOT NULL COMMENT '수입/지출 (INCOME/EXPENSE)',
    icon VARCHAR(50) COMMENT '아이콘 이름',
    color VARCHAR(20) COMMENT '색상 코드',
    display_order INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 거래 내역 테이블
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT COMMENT '카테고리 ID (NULL 허용)',
    amount DECIMAL(15, 2) NOT NULL COMMENT '금액',
    type VARCHAR(10) NOT NULL COMMENT '수입/지출 (INCOME/EXPENSE)',
    description VARCHAR(255) COMMENT '설명/메모',
    transaction_date DATE NOT NULL COMMENT '거래 날짜',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

