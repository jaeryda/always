package com.always.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    private Long id;
    
    private Long userId;
    
    private Long categoryId;
    
    @JsonIgnoreProperties({"userId", "createdAt", "updatedAt"})
    private Category category;  // 카테고리 정보
    
    private BigDecimal amount;  // 금액
    
    private String type;  // INCOME(수입) 또는 EXPENSE(지출)
    
    private String description;  // 설명/메모
    
    private LocalDate transactionDate;  // 거래 날짜
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

