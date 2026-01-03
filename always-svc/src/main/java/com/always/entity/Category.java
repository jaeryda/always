package com.always.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    private Long id;
    
    private Long userId;
    
    private String name;  // 카테고리명 (예: 식비, 교통비, 쇼핑)
    
    private String type;  // INCOME(수입) 또는 EXPENSE(지출)
    
    private String icon;  // 아이콘 이름
    
    private String color;  // 색상 코드 (#FF5733 등)
    
    private Integer displayOrder;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

