-- MySQL 데이터베이스 생성 스크립트
-- MySQL에 접속한 후 이 스크립트를 실행하세요

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS always_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 데이터베이스 선택
USE always_db;

-- 사용자 생성 (선택사항 - root 대신 별도 사용자 사용 시)
-- CREATE USER 'always_user'@'localhost' IDENTIFIED BY 'always_password';
-- GRANT ALL PRIVILEGES ON always_db.* TO 'always_user'@'localhost';
-- FLUSH PRIVILEGES;


