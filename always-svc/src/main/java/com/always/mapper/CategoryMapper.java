package com.always.mapper;

import com.always.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    
    // 전체 조회 (사용자별)
    List<Category> findAllByUserId(@Param("userId") Long userId);
    
    // 타입별 조회 (수입/지출)
    List<Category> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    // ID로 조회
    Category findById(@Param("id") Long id);
    
    // 사용자 ID와 카테고리 ID로 조회
    Category findByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);
    
    // 카테고리명으로 조회 (중복 체크용)
    Category findByUserIdAndNameAndType(@Param("userId") Long userId, 
                                        @Param("name") String name, 
                                        @Param("type") String type);
    
    // 삽입
    void insert(Category category);
    
    // 수정
    void update(Category category);
    
    // 삭제
    void deleteById(@Param("id") Long id);
    
    // 사용자별 카테고리 개수
    int countByUserId(@Param("userId") Long userId);
}

