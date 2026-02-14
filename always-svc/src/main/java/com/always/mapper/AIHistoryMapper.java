package com.always.mapper;

import com.always.entity.AIHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AIHistoryMapper {
    List<AIHistory> findByUserId(@Param("userId") Long userId);
    void insert(AIHistory history);
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
    int deleteAllByUserId(@Param("userId") Long userId);
}

