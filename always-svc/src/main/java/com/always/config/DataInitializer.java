package com.always.config;

import com.always.entity.Menu;
import com.always.entity.Post;
import com.always.entity.User;
import com.always.entity.Category;
import com.always.mapper.MenuMapper;
import com.always.mapper.PostMapper;
import com.always.mapper.UserMapper;
import com.always.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PostMapper postMapper;
    private final MenuMapper menuMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DataInitializer(PostMapper postMapper, MenuMapper menuMapper, 
                          UserMapper userMapper, CategoryMapper categoryMapper,
                          PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.postMapper = postMapper;
        this.menuMapper = menuMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ensureFeatureTables();
        // 초기 메뉴 데이터 생성 (메뉴가 없을 때만 전체 생성)
        if (menuMapper.count() == 0) {
            Menu menu1 = new Menu();
            menu1.setName("홈");
            menu1.setPath("/");
            menu1.setIcon("House");
            menu1.setDisplayOrder(1);
            menu1.setVisible(true);
            menu1.setCreatedAt(LocalDateTime.now());
            menu1.setUpdatedAt(LocalDateTime.now());

            Menu menu2 = new Menu();
            menu2.setName("포스트");
            menu2.setPath("/posts");
            menu2.setIcon("Document");
            menu2.setDisplayOrder(2);
            menu2.setVisible(true);
            menu2.setCreatedAt(LocalDateTime.now());
            menu2.setUpdatedAt(LocalDateTime.now());

            Menu menu3 = new Menu();
            menu3.setName("AI 채팅");
            menu3.setPath("/ai");
            menu3.setIcon("ChatDotRound");
            menu3.setDisplayOrder(3);
            menu3.setVisible(true);
            menu3.setCreatedAt(LocalDateTime.now());
            menu3.setUpdatedAt(LocalDateTime.now());

            Menu menu4 = new Menu();
            menu4.setName("About");
            menu4.setPath("/about");
            menu4.setIcon("InfoFilled");
            menu4.setDisplayOrder(4);
            menu4.setVisible(true);
            menu4.setCreatedAt(LocalDateTime.now());
            menu4.setUpdatedAt(LocalDateTime.now());

            Menu menu5 = new Menu();
            menu5.setName("가계부");
            menu5.setPath("/account-book");
            menu5.setIcon("Wallet");
            menu5.setDisplayOrder(5);
            menu5.setVisible(true);
            menu5.setCreatedAt(LocalDateTime.now());
            menu5.setUpdatedAt(LocalDateTime.now());

            Menu menu6 = new Menu();
            menu6.setName("AI 이미지 생성");
            menu6.setPath("/image-generator");
            menu6.setIcon("Camera");  // Picture 대신 Camera 사용
            menu6.setDisplayOrder(6);
            menu6.setVisible(true);
            menu6.setCreatedAt(LocalDateTime.now());
            menu6.setUpdatedAt(LocalDateTime.now());

            Menu menu7 = new Menu();
            menu7.setName("AI 동영상 생성");
            menu7.setPath("/video-generator");
            menu7.setIcon("VideoCamera");
            menu7.setDisplayOrder(7);
            menu7.setVisible(true);
            menu7.setCreatedAt(LocalDateTime.now());
            menu7.setUpdatedAt(LocalDateTime.now());

            menuMapper.insert(menu1);
            menuMapper.insert(menu2);
            menuMapper.insert(menu3);
            menuMapper.insert(menu4);
            menuMapper.insert(menu5);
            menuMapper.insert(menu6);
            menuMapper.insert(menu7);

            System.out.println("초기 메뉴 데이터가 생성되었습니다.");
        } else {
            // 기존 메뉴가 있는 경우, AI 채팅 메뉴와 가계부 메뉴가 없으면 추가
            List<Menu> allMenus = menuMapper.findAllOrderByDisplayOrder();
            
            boolean aiMenuExists = allMenus.stream()
                    .anyMatch(menu -> "/ai".equals(menu.getPath()));
            if (!aiMenuExists) {
                Menu aiMenu = new Menu();
                aiMenu.setName("AI 채팅");
                aiMenu.setPath("/ai");
                aiMenu.setIcon("ChatDotRound");
                aiMenu.setDisplayOrder(3);
                aiMenu.setVisible(true);
                aiMenu.setCreatedAt(LocalDateTime.now());
                aiMenu.setUpdatedAt(LocalDateTime.now());
                menuMapper.insert(aiMenu);
                System.out.println("AI 채팅 메뉴가 추가되었습니다.");
            }
            
            boolean accountBookMenuExists = allMenus.stream()
                    .anyMatch(menu -> "/account-book".equals(menu.getPath()));
            if (!accountBookMenuExists) {
                // 기존 메뉴의 최대 displayOrder를 찾아서 다음 순서로 설정
                int maxOrder = allMenus.stream()
                        .mapToInt(Menu::getDisplayOrder)
                        .max()
                        .orElse(0);
                
                Menu accountBookMenu = new Menu();
                accountBookMenu.setName("가계부");
                accountBookMenu.setPath("/account-book");
                accountBookMenu.setIcon("Wallet");
                accountBookMenu.setDisplayOrder(maxOrder + 1);
                accountBookMenu.setVisible(true);
                accountBookMenu.setCreatedAt(LocalDateTime.now());
                accountBookMenu.setUpdatedAt(LocalDateTime.now());
                menuMapper.insert(accountBookMenu);
                System.out.println("가계부 메뉴가 추가되었습니다.");
            }
            
            boolean imageGeneratorMenuExists = allMenus.stream()
                    .anyMatch(menu -> "/image-generator".equals(menu.getPath()));
            if (!imageGeneratorMenuExists) {
                // 기존 메뉴의 최대 displayOrder를 찾아서 다음 순서로 설정
                List<Menu> updatedMenus = menuMapper.findAllOrderByDisplayOrder();
                int maxOrder = updatedMenus.stream()
                        .mapToInt(Menu::getDisplayOrder)
                        .max()
                        .orElse(0);
                
                Menu imageGeneratorMenu = new Menu();
                imageGeneratorMenu.setName("AI 이미지 생성");
                imageGeneratorMenu.setPath("/image-generator");
                imageGeneratorMenu.setIcon("Camera");  // Picture 대신 Camera 사용
                imageGeneratorMenu.setDisplayOrder(maxOrder + 1);
                imageGeneratorMenu.setVisible(true);
                imageGeneratorMenu.setCreatedAt(LocalDateTime.now());
                imageGeneratorMenu.setUpdatedAt(LocalDateTime.now());
                menuMapper.insert(imageGeneratorMenu);
                System.out.println("AI 이미지 생성 메뉴가 추가되었습니다.");
            }
            
            boolean videoGeneratorMenuExists = allMenus.stream()
                    .anyMatch(menu -> "/video-generator".equals(menu.getPath()));
            if (!videoGeneratorMenuExists) {
                // 기존 메뉴의 최대 displayOrder를 찾아서 다음 순서로 설정
                List<Menu> updatedMenus2 = menuMapper.findAllOrderByDisplayOrder();
                int maxOrder2 = updatedMenus2.stream()
                        .mapToInt(Menu::getDisplayOrder)
                        .max()
                        .orElse(0);
                
                Menu videoGeneratorMenu = new Menu();
                videoGeneratorMenu.setName("AI 동영상 생성");
                videoGeneratorMenu.setPath("/video-generator");
                videoGeneratorMenu.setIcon("VideoCamera");
                videoGeneratorMenu.setDisplayOrder(maxOrder2 + 1);
                videoGeneratorMenu.setVisible(true);
                videoGeneratorMenu.setCreatedAt(LocalDateTime.now());
                videoGeneratorMenu.setUpdatedAt(LocalDateTime.now());
                menuMapper.insert(videoGeneratorMenu);
                System.out.println("AI 동영상 생성 메뉴가 추가되었습니다.");
            }
        }

        // 기본 사용자 생성 (없으면)
        User defaultUser = userMapper.findByUsername("admin");
        if (defaultUser == null) {
            defaultUser = new User();
            defaultUser.setUsername("admin");
            defaultUser.setEmail("admin@example.com");
            defaultUser.setPassword(passwordEncoder.encode("admin123"));
            defaultUser.setLoginType("email");
            defaultUser.setRole("ADMIN");
            defaultUser.setCreatedAt(LocalDateTime.now());
            defaultUser.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(defaultUser);
        } else {
            if (defaultUser.getRole() == null || defaultUser.getRole().isBlank()) {
                defaultUser.setRole("ADMIN");
                defaultUser.setUpdatedAt(LocalDateTime.now());
                userMapper.update(defaultUser);
            }
        }

        // 기존 Post 데이터의 author_id를 정리 (외래 키 제약 조건 해결)
        try {
            // 외래 키 체크를 일시적으로 비활성화하여 데이터 정리
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            try {
                // author_id 컬럼을 NULL 허용하도록 변경 (NOT NULL 제약 조건 제거)
                try {
                    jdbcTemplate.execute("ALTER TABLE posts MODIFY COLUMN author_id BIGINT NULL");
                    System.out.println("author_id 컬럼을 NULL 허용하도록 변경했습니다.");
                } catch (Exception e) {
                    // 컬럼이 이미 NULL 허용이거나 다른 이유로 실패할 수 있음
                    System.out.println("author_id 컬럼 변경 건너뜀 (이미 NULL 허용일 수 있음): " + e.getMessage());
                }
                
                // author_id가 0인 경우를 NULL로 설정
                try {
                    int updated = jdbcTemplate.update("UPDATE posts SET author_id = NULL WHERE author_id = 0");
                    if (updated > 0) {
                        System.out.println("유효하지 않은 author_id를 NULL로 설정한 포스트: " + updated + "개");
                    }
                } catch (Exception e) {
                    System.out.println("author_id NULL 설정 건너뜀: " + e.getMessage());
                }
            } finally {
                // 외래 키 체크 다시 활성화
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        } catch (Exception e) {
            // 컬럼이 아직 없거나 다른 이유로 실패할 수 있으므로 무시하고 계속 진행
            System.out.println("author_id 컬럼 초기화 건너뜀: " + e.getMessage());
            // 외래 키 체크가 비활성화된 상태일 수 있으므로 다시 활성화
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ex) {
                // 무시
            }
        }

        // 기존 Post 데이터에 author가 없는 경우 기본 사용자 할당
        try {
            List<Post> allPosts = postMapper.findAll();
            int fixedCount = 0;
            for (Post post : allPosts) {
                try {
                    User author = post.getAuthor();
                    if (author == null || author.getId() == null) {
                        post.setAuthor(defaultUser);
                        post.setUpdatedAt(LocalDateTime.now());
                        postMapper.update(post);
                        fixedCount++;
                    }
                } catch (Exception e) {
                    // author가 없는 경우 기본 사용자 할당
                    try {
                        post.setAuthor(defaultUser);
                        post.setUpdatedAt(LocalDateTime.now());
                        postMapper.update(post);
                        fixedCount++;
                    } catch (Exception ex) {
                        System.err.println("포스트 ID " + post.getId() + " 작성자 할당 실패: " + ex.getMessage());
                    }
                }
            }
            if (fixedCount > 0) {
                System.out.println("기존 포스트 " + fixedCount + "개에 작성자를 할당했습니다.");
            }
        } catch (Exception e) {
            System.err.println("기존 포스트 작성자 할당 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        // 데이터베이스가 비어있으면 초기 데이터 추가
        if (postMapper.count() == 0) {
            Post post1 = new Post();
            post1.setTitle("첫 번째 포스트");
            post1.setContent("안녕하세요! 이것은 첫 번째 포스트입니다.");
            post1.setAuthor(defaultUser);
            post1.setCreatedAt(LocalDateTime.now());
            post1.setUpdatedAt(LocalDateTime.now());

            Post post2 = new Post();
            post2.setTitle("Vue와 Spring Boot 연동");
            post2.setContent("Vue 프론트엔드와 Spring Boot 백엔드를 연동했습니다.");
            post2.setAuthor(defaultUser);
            post2.setCreatedAt(LocalDateTime.now());
            post2.setUpdatedAt(LocalDateTime.now());

            Post post3 = new Post();
            post3.setTitle("MySQL 데이터베이스 연동");
            post3.setContent("Spring Boot MyBatis를 사용하여 MySQL 데이터베이스와 연동했습니다.");
            post3.setAuthor(defaultUser);
            post3.setCreatedAt(LocalDateTime.now());
            post3.setUpdatedAt(LocalDateTime.now());

            postMapper.insert(post1);
            postMapper.insert(post2);
            postMapper.insert(post3);

            System.out.println("초기 포스트 데이터가 생성되었습니다.");
        }

        // 기본 카테고리 데이터 초기화 (각 사용자별로)
        try {
            List<User> allUsers = userMapper.findAll();
            for (User user : allUsers) {
                // 사용자별 카테고리 개수 확인
                int categoryCount = categoryMapper.countByUserId(user.getId());
                if (categoryCount == 0) {
                    // 기본 지출 카테고리
                    Category category1 = new Category();
                    category1.setUserId(user.getId());
                    category1.setName("식비");
                    category1.setType("EXPENSE");
                    category1.setColor("#f56c6c");
                    category1.setDisplayOrder(1);
                    category1.setCreatedAt(LocalDateTime.now());
                    category1.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(category1);

                    Category category2 = new Category();
                    category2.setUserId(user.getId());
                    category2.setName("교통비");
                    category2.setType("EXPENSE");
                    category2.setColor("#409eff");
                    category2.setDisplayOrder(2);
                    category2.setCreatedAt(LocalDateTime.now());
                    category2.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(category2);

                    Category category3 = new Category();
                    category3.setUserId(user.getId());
                    category3.setName("쇼핑");
                    category3.setType("EXPENSE");
                    category3.setColor("#e6a23c");
                    category3.setDisplayOrder(3);
                    category3.setCreatedAt(LocalDateTime.now());
                    category3.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(category3);

                    Category category4 = new Category();
                    category4.setUserId(user.getId());
                    category4.setName("의료비");
                    category4.setType("EXPENSE");
                    category4.setColor("#67c23a");
                    category4.setDisplayOrder(4);
                    category4.setCreatedAt(LocalDateTime.now());
                    category4.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(category4);

                    Category category5 = new Category();
                    category5.setUserId(user.getId());
                    category5.setName("기타");
                    category5.setType("EXPENSE");
                    category5.setColor("#909399");
                    category5.setDisplayOrder(5);
                    category5.setCreatedAt(LocalDateTime.now());
                    category5.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(category5);

                    // 기본 수입 카테고리
                    Category incomeCategory1 = new Category();
                    incomeCategory1.setUserId(user.getId());
                    incomeCategory1.setName("월급");
                    incomeCategory1.setType("INCOME");
                    incomeCategory1.setColor("#67c23a");
                    incomeCategory1.setDisplayOrder(1);
                    incomeCategory1.setCreatedAt(LocalDateTime.now());
                    incomeCategory1.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(incomeCategory1);

                    Category incomeCategory2 = new Category();
                    incomeCategory2.setUserId(user.getId());
                    incomeCategory2.setName("용돈");
                    incomeCategory2.setType("INCOME");
                    incomeCategory2.setColor("#409eff");
                    incomeCategory2.setDisplayOrder(2);
                    incomeCategory2.setCreatedAt(LocalDateTime.now());
                    incomeCategory2.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(incomeCategory2);

                    Category incomeCategory3 = new Category();
                    incomeCategory3.setUserId(user.getId());
                    incomeCategory3.setName("기타 수입");
                    incomeCategory3.setType("INCOME");
                    incomeCategory3.setColor("#909399");
                    incomeCategory3.setDisplayOrder(3);
                    incomeCategory3.setCreatedAt(LocalDateTime.now());
                    incomeCategory3.setUpdatedAt(LocalDateTime.now());
                    categoryMapper.insert(incomeCategory3);

                    System.out.println("사용자 " + user.getUsername() + "의 기본 카테고리가 생성되었습니다.");
                }
            }
        } catch (Exception e) {
            System.err.println("기본 카테고리 초기화 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureFeatureTables() {
        try {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'USER'");
        } catch (Exception ignored) {
            // already exists
        }
        jdbcTemplate.update("UPDATE users SET role = 'USER' WHERE role IS NULL OR role = ''");
        jdbcTemplate.update("UPDATE users SET role = 'ADMIN' WHERE username = 'admin'");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS post_reactions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                post_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                type VARCHAR(20) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                UNIQUE (post_id, user_id, type)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS post_comments (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                post_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS monthly_budgets (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                category_id BIGINT NOT NULL,
                year INT NOT NULL,
                month INT NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL,
                UNIQUE (user_id, category_id, year, month)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS app_notifications (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(255) NOT NULL,
                message TEXT,
                is_read BOOLEAN NOT NULL DEFAULT FALSE,
                created_at TIMESTAMP NOT NULL
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS ai_history (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                type VARCHAR(20) NOT NULL,
                prompt TEXT,
                result_text TEXT,
                result_url TEXT,
                created_at TIMESTAMP NOT NULL
            )
        """);
    }
}
