package com.always.mapper;

import com.always.entity.UserLoginHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserLoginHistoryMapper {
    
    // 저장
    void insert(UserLoginHistory history);
    
    // user_id로 조회 (최근순)
    List<UserLoginHistory> findByUserId(Long userId);
    
    // user_id와 action_type으로 조회
    List<UserLoginHistory> findByUserIdAndActionType(Long userId, String actionType);
    
    // 전체 조회 (최근순)
    List<UserLoginHistory> findAll();
}




