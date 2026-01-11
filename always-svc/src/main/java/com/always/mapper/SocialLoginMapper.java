package com.always.mapper;

import com.always.entity.SocialLogin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface SocialLoginMapper {
    
    // provider와 providerUserId로 조회
    SocialLogin findByProviderAndProviderUserId(@Param("provider") String provider, 
                                                @Param("providerUserId") String providerUserId);
    
    // provider와 email로 조회
    SocialLogin findByProviderAndEmail(@Param("provider") String provider, 
                                       @Param("email") String email);
    
    // 저장
    void insert(SocialLogin socialLogin);
    
    // user_id로 조회 (카카오 로그인 사용자인지 확인용)
    SocialLogin findByUserId(@Param("userId") Long userId);
    
    // 삭제
    void deleteById(Long id);
    
    // user_id로 삭제
    void deleteByUserId(Long userId);
}

