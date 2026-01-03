package com.always.mapper;

import com.always.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    
    // 전체 조회
    List<User> findAll();
    
    // ID로 조회
    User findById(Long id);
    
    // 사용자명으로 조회
    User findByUsername(String username);
    
    // 이메일로 조회
    User findByEmail(String email);
    
    // 사용자명 존재 여부
    boolean existsByUsername(String username);
    
    // 이메일 존재 여부
    boolean existsByEmail(String email);
    
    // 저장
    void insert(User user);
    
    // 업데이트
    void update(User user);
    
    // 삭제
    void deleteById(Long id);
    
    // 전체 개수
    int count();
}

