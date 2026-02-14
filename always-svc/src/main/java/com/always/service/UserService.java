package com.always.service;

import com.always.entity.SocialLogin;
import com.always.entity.User;
import com.always.mapper.SocialLoginMapper;
import com.always.mapper.UserMapper;
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
        if (userMapper.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");
        }
        if (userMapper.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getLoginType() == null || user.getLoginType().trim().isEmpty()) {
            user.setLoginType("email");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("USER");
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userMapper.findByUsername(username));
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.findById(id));
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userMapper.findByEmail(email));
    }

    public User findKakaoUser(String email, String kakaoId) {
        String provider = "kakao";

        SocialLogin socialLogin = socialLoginMapper.findByProviderAndProviderUserId(provider, kakaoId);
        if (socialLogin != null) {
            Optional<User> userOpt = findById(socialLogin.getUserId());
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }

        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLoginType(provider);
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("USER");
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);

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

        return null;
    }

    public User createKakaoUser(String email, String nickname, String kakaoId, String username) {
        String provider = "kakao";

        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(email);

        String finalUsername;
        if (username != null && !username.trim().isEmpty()) {
            finalUsername = username.trim();
        } else {
            finalUsername = (nickname != null && !nickname.trim().isEmpty()) ? nickname.trim() : email.split("@")[0];
        }

        String baseUsername = finalUsername;
        int counter = 1;
        while (userMapper.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }

        user.setUsername(finalUsername);
        user.setPassword(passwordEncoder.encode(String.valueOf(System.currentTimeMillis())));
        user.setLoginType(provider);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

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

    public boolean isKakaoUser(Long userId) {
        SocialLogin socialLogin = socialLoginMapper.findByUserId(userId);
        return socialLogin != null && "kakao".equals(socialLogin.getProvider());
    }

    public User findNaverUser(String email, String naverId) {
        String provider = "naver";

        SocialLogin socialLogin = socialLoginMapper.findByProviderAndProviderUserId(provider, naverId);
        if (socialLogin != null) {
            Optional<User> userOpt = findById(socialLogin.getUserId());
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }

        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLoginType(provider);
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("USER");
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);

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

        return null;
    }

    public User createNaverUser(String email, String nickname, String naverId, String username) {
        String provider = "naver";

        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User();
        user.setEmail(email);

        String finalUsername;
        if (username != null && !username.trim().isEmpty()) {
            finalUsername = username.trim();
        } else {
            finalUsername = (nickname != null && !nickname.trim().isEmpty()) ? nickname.trim() : email.split("@")[0];
        }

        String baseUsername = finalUsername;
        int counter = 1;
        while (userMapper.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }

        user.setUsername(finalUsername);
        user.setPassword(passwordEncoder.encode(String.valueOf(System.currentTimeMillis())));
        user.setLoginType(provider);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

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