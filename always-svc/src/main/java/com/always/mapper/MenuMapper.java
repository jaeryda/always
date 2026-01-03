package com.always.mapper;

import com.always.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {
    
    // 전체 조회 (순서대로)
    List<Menu> findAllOrderByDisplayOrder();
    
    // 표시 가능한 메뉴 조회 (순서대로)
    List<Menu> findVisibleMenusOrderByDisplayOrder();
    
    // ID로 조회
    Menu findById(Long id);
    
    // 저장
    void insert(Menu menu);
    
    // 업데이트
    void update(Menu menu);
    
    // 삭제
    void deleteById(Long id);
    
    // 존재 여부 확인
    boolean existsById(Long id);
    
    // 전체 개수
    int count();
}

