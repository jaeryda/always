package com.always.service;

import com.always.entity.User;
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
}
