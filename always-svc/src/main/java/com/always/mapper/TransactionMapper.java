package com.always.mapper;

import com.always.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TransactionMapper {
    
    // 전체 조회 (사용자별, 최신순)
    List<Transaction> findAllByUserId(@Param("userId") Long userId);
    
    // ID로 조회
    Transaction findById(@Param("id") Long id);
    
    // 사용자 ID와 거래 ID로 조회
    Transaction findByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);
    
    // 날짜 범위로 조회
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    // 월별 조회
    List<Transaction> findByUserIdAndYearMonth(@Param("userId") Long userId,
                                               @Param("year") int year,
                                               @Param("month") int month);
    
    // 카테고리별 조회
    List<Transaction> findByUserIdAndCategoryId(@Param("userId") Long userId,
                                                @Param("categoryId") Long categoryId);
    
    // 타입별 조회 (수입/지출)
    List<Transaction> findByUserIdAndType(@Param("userId") Long userId,
                                          @Param("type") String type);
    
    // 삽입
    void insert(Transaction transaction);
    
    // 수정
    void update(Transaction transaction);
    
    // 삭제
    void deleteById(@Param("id") Long id);
    
    // 사용자별 거래 개수
    int countByUserId(@Param("userId") Long userId);
    
    // 월별 합계 (수입/지출)
    BigDecimal sumByUserIdAndTypeAndYearMonth(@Param("userId") Long userId,
                                              @Param("type") String type,
                                              @Param("year") int year,
                                              @Param("month") int month);
    
    // 카테고리별 합계
    BigDecimal sumByUserIdAndCategoryIdAndYearMonth(@Param("userId") Long userId,
                                                    @Param("categoryId") Long categoryId,
                                                    @Param("year") int year,
                                                    @Param("month") int month);
}

