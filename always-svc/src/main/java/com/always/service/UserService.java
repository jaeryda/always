package com.always.service;

import com.always.entity.User;
import com.always.entity.SocialLogin;
import com.always.mapper.UserMapper;
import com.always.mapper.SocialLoginMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SocialLoginMapper socialLoginMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        // 중복 체크
        if (userMapper.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }
        if (userMapper.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 비밀번호 해싱
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        // loginType 설정 (기본값: email)
        if (user.getLoginType() == null || user.getLoginType().trim().isEmpty()) {
            user.setLoginType("email");
        }
        
        // createdAt, updatedAt 설정
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return Optional.ofNullable(user);
    }

    public Optional<User> findById(Long id) {
        User user = userMapper.findById(id);
        return Optional.ofNullable(user);
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public boolean validatePassword(User user, String rawPassword) {
        // BCrypt로 해시된 비밀번호 검증
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public Optional<User> findByEmail(String email) {
        User user = userMapper.findByEmail(email);
        return Optional.ofNullable(user);
    }

    /**
     * 카카오 로그인용 사용자 생성 또는 조회
     * 이메일이 없으면 닉네임으로 사용자명 생성
     */
    /**
     * 카카오 로그인 사용자 찾기 (생성하지 않음)
     * 
     * 1. 소셜 로그인 테이블에 있으면 → 로그인 (user_id로 users 테이블 조회)
     * 2. 소셜 로그인 테이블에 없고 users 테이블에 email이 있으면 → 소셜 로그인 연동
     * 3. users 테이블에도 없으면 → null 반환 (가입 필요)
     * 
     * @param email 카카오 이메일
     * @param nickname 카카오 닉네임
     * @param kakaoId 카카오 사용자 ID
     * @return User 객체 (없으면 null)
     */
    public User findKakaoUser(String email, String kakaoId) {
        String provider = "kakao";
        
        // 1. 소셜 로그인 테이블에서 provider와 providerUserId로 조회
        SocialLogin socialLogin = socialLoginMapper.findByProviderAndProviderUserId(provider, kakaoId);
        
        if (socialLogin != null) {
            // 소셜 로그인 테이블에 있으면 → 로그인 (user_id로 users 테이블 조회)
            Optional<User> userOpt = findById(socialLogin.getUserId());
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        
        // 2. users 테이블에 email로 조회
        Optional<User> userOpt = findByEmail(email);
        User user;
        
        if (userOpt.isPresent()) {
            // users 테이블에 있으면 → 소셜 로그인 연동
            user = userOpt.get();
            
            // loginType 업데이트 (기존 사용자가 소셜 로그인을 연동하는 경우)
            user.setLoginType(provider);  // "kakao"
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            
            // 소셜 로그인 테이블에 저장
            SocialLogin newSocialLogin = new SocialLogin();
            newSocialLogin.setUserId(user.getId());
            newSocialLogin.setProvider(provider);
            newSocialLogin.setProviderUserId(kakaoId);
            newSocialLogin.setEmail(email);
            newSocialLogin.setCreatedAt(LocalDateTime.now());
            newSocialLogin.setUpdatedAt(LocalDateTime.now());
            socialLoginMapper.insert(newSocialLogin);
            
            return user;
            
        } else {
            // 3. users 테이블에도 없으면 → null 반환 (가입 필요)
            return null;
        }
    }
    
    /**
     * 카카오 로그인 사용자 생성 (신규 가입)
     * 
     * @param email 카카오 이메일
     * @param nickname 카카오 닉네임
     * @param kakaoId 카카오 사용자 ID
     * @param username 사용자명 (선택사항, null이면 nickname 또는 email로 자동 생성)
     * @return 생성된 User 객체
     */
    public User createKakaoUser(String email, String nickname, String kakaoId, String username) {
        String provider = "kakao";
        
        // users 테이블에 이미 존재하는지 확인
        Optional<User> existingUserOpt = findByEmail(email);
        if (existingUserOpt.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        User user = new User();
        user.setEmail(email);
        
        // 사용자명 생성 (전달받은 username이 있으면 사용, 없으면 닉네임 또는 이메일 앞부분 사용)
        String finalUsername;
        if (username != null && !username.trim().isEmpty()) {
            finalUsername = username.trim();
        } else {
            finalUsername = nickname != null && !nickname.trim().isEmpty() 
                ? nickname.trim() 
                : email.split("@")[0];
        }
        
        // 사용자명 중복 체크 및 처리
        String baseUsername = finalUsername;
        int counter = 1;
        while (userMapper.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }
        
        user.setUsername(finalUsername);
        
        // 카카오 로그인 사용자는 비밀번호를 랜덤하게 생성 (실제로는 사용하지 않음)
        String randomPassword = passwordEncoder.encode(String.valueOf(System.currentTimeMillis()));
        user.setPassword(randomPassword);
        
        // loginType 설정
        user.setLoginType(provider);  // "kakao"
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // users 테이블에 저장
        userMapper.insert(user);
        
        // social_logins 테이블에 저장
        SocialLogin newSocialLogin = new SocialLogin();
        newSocialLogin.setUserId(user.getId());
        newSocialLogin.setProvider(provider);
        newSocialLogin.setProviderUserId(kakaoId);
        newSocialLogin.setEmail(email);
        newSocialLogin.setCreatedAt(LocalDateTime.now());
        newSocialLogin.setUpdatedAt(LocalDateTime.now());
        socialLoginMapper.insert(newSocialLogin);
        
        return user;
    }
    
    /**
     * 카카오 로그인 사용자인지 확인
     * 
     * @param userId 사용자 ID
     * @return 카카오 로그인 사용자 여부
     */
    public boolean isKakaoUser(Long userId) {
        SocialLogin socialLogin = socialLoginMapper.findByUserId(userId);
        return socialLogin != null && "kakao".equals(socialLogin.getProvider());
    }

    /**
     * 네이버 로그인 사용자 찾기 (생성하지 않음)
     * 
     * 1. 소셜 로그인 테이블에 있으면 → 로그인 (user_id로 users 테이블 조회)
     * 2. 소셜 로그인 테이블에 없고 users 테이블에 email이 있으면 → 소셜 로그인 연동
     * 3. users 테이블에도 없으면 → null 반환 (가입 필요)
     * 
     * @param email 네이버 이메일
     * @param naverId 네이버 사용자 ID
     * @return User 객체 (없으면 null)
     */
    public User findNaverUser(String email, String naverId) {
        String provider = "naver";
        
        // 1. 소셜 로그인 테이블에서 provider와 providerUserId로 조회
        SocialLogin socialLogin = socialLoginMapper.findByProviderAndProviderUserId(provider, naverId);
        
        if (socialLogin != null) {
            // 소셜 로그인 테이블에 있으면 → 로그인 (user_id로 users 테이블 조회)
            Optional<User> userOpt = findById(socialLogin.getUserId());
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        
        // 2. users 테이블에 email로 조회
        Optional<User> userOpt = findByEmail(email);
        User user;
        
        if (userOpt.isPresent()) {
            // users 테이블에 있으면 → 소셜 로그인 연동
            user = userOpt.get();
            
            // loginType 업데이트 (기존 사용자가 소셜 로그인을 연동하는 경우)
            user.setLoginType(provider);  // "naver"
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            
            // 소셜 로그인 테이블에 저장
            SocialLogin newSocialLogin = new SocialLogin();
            newSocialLogin.setUserId(user.getId());
            newSocialLogin.setProvider(provider);
            newSocialLogin.setProviderUserId(naverId);
            newSocialLogin.setEmail(email);
            newSocialLogin.setCreatedAt(LocalDateTime.now());
            newSocialLogin.setUpdatedAt(LocalDateTime.now());
            socialLoginMapper.insert(newSocialLogin);
            
            return user;
            
        } else {
            // 3. users 테이블에도 없으면 → null 반환 (가입 필요)
            return null;
        }
    }
    
    /**
     * 네이버 로그인 사용자 생성 (신규 가입)
     * 
     * @param email 네이버 이메일
     * @param nickname 네이버 닉네임
     * @param naverId 네이버 사용자 ID
     * @param username 사용자명 (선택사항, null이면 nickname 또는 email로 자동 생성)
     * @return 생성된 User 객체
     */
    public User createNaverUser(String email, String nickname, String naverId, String username) {
        String provider = "naver";
        
        // users 테이블에 이미 존재하는지 확인
        Optional<User> existingUserOpt = findByEmail(email);
        if (existingUserOpt.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        
        User user = new User();
        user.setEmail(email);
        
        // 사용자명 생성 (전달받은 username이 있으면 사용, 없으면 닉네임 또는 이메일 앞부분 사용)
        String finalUsername;
        if (username != null && !username.trim().isEmpty()) {
            finalUsername = username.trim();
        } else {
            finalUsername = nickname != null && !nickname.trim().isEmpty() 
                ? nickname.trim() 
                : email.split("@")[0];
        }
        
        // 사용자명 중복 체크 및 처리
        String baseUsername = finalUsername;
        int counter = 1;
        while (userMapper.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }
        
        user.setUsername(finalUsername);
        
        // 네이버 로그인 사용자는 비밀번호를 랜덤하게 생성 (실제로는 사용하지 않음)
        String randomPassword = passwordEncoder.encode(String.valueOf(System.currentTimeMillis()));
        user.setPassword(randomPassword);
        
        // loginType 설정
        user.setLoginType(provider);  // "naver"
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // users 테이블에 저장
        userMapper.insert(user);
        
        // social_logins 테이블에 저장
        SocialLogin newSocialLogin = new SocialLogin();
        newSocialLogin.setUserId(user.getId());
        newSocialLogin.setProvider(provider);
        newSocialLogin.setProviderUserId(naverId);
        newSocialLogin.setEmail(email);
        newSocialLogin.setCreatedAt(LocalDateTime.now());
        newSocialLogin.setUpdatedAt(LocalDateTime.now());
        socialLoginMapper.insert(newSocialLogin);
        
        return user;
    }
}
