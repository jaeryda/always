-- AI 이미지 생성 메뉴 추가 SQL 스크립트
-- 이미지 생성 메뉴가 없을 경우에만 추가합니다.

-- 메뉴가 이미 존재하는지 확인 후 추가
-- display_order는 기존 메뉴의 최대값 + 1로 설정
INSERT INTO menus (name, path, icon, display_order, visible, created_at, updated_at)
SELECT 
    'AI 이미지 생성' AS name,
    '/image-generator' AS path,
    'Picture' AS icon,
    COALESCE((SELECT MAX(display_order) FROM menus), 0) + 1 AS display_order,
    true AS visible,
    NOW() AS created_at,
    NOW() AS updated_at
WHERE NOT EXISTS (
    SELECT 1 FROM menus WHERE path = '/image-generator'
);

-- 결과 확인
SELECT * FROM menus WHERE path = '/image-generator';

-- 전체 메뉴 목록 확인 (순서대로)
SELECT id, name, path, icon, display_order, visible 
FROM menus 
ORDER BY display_order ASC;

