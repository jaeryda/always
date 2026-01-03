-- author_id 컬럼 외래 키 제약 조건 문제 해결 스크립트
-- MySQL에 접속한 후 always_db 데이터베이스를 선택하고 실행하세요

USE always_db;

-- 1. 외래 키 체크를 일시적으로 비활성화
SET FOREIGN_KEY_CHECKS = 0;

-- 2. author_id 컬럼을 NULL 허용하도록 변경 (NOT NULL 제약 조건 제거)
ALTER TABLE posts MODIFY COLUMN author_id BIGINT NULL;

-- 3. 유효하지 않은 author_id 값을 NULL로 설정
UPDATE posts SET author_id = NULL WHERE author_id = 0;

-- 4. users 테이블에 존재하지 않는 author_id를 NULL로 설정
-- (author_id가 NULL이 아닌데 users 테이블에 해당 ID가 없는 경우)
UPDATE posts p 
LEFT JOIN users u ON p.author_id = u.id 
SET p.author_id = NULL 
WHERE p.author_id IS NOT NULL AND u.id IS NULL;

-- 5. 외래 키 체크 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;

-- 6. 확인: author_id가 NULL인 포스트 확인
SELECT id, title, author_id FROM posts WHERE author_id IS NULL;

