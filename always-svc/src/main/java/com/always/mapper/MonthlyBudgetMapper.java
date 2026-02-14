package com.always.mapper;

import com.always.entity.MonthlyBudget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MonthlyBudgetMapper {
    List<MonthlyBudget> findByUserIdAndYearMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    MonthlyBudget findByUniqueKey(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("year") int year, @Param("month") int month);
    void insert(MonthlyBudget budget);
    void update(MonthlyBudget budget);
    int deleteByUniqueKey(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("year") int year, @Param("month") int month);
}

