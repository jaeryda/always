package com.always.service;

import com.always.entity.MonthlyBudget;
import com.always.mapper.MonthlyBudgetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MonthlyBudgetService {

    private final MonthlyBudgetMapper monthlyBudgetMapper;

    @Autowired
    public MonthlyBudgetService(MonthlyBudgetMapper monthlyBudgetMapper) {
        this.monthlyBudgetMapper = monthlyBudgetMapper;
    }

    public List<MonthlyBudget> getBudgets(Long userId, int year, int month) {
        return monthlyBudgetMapper.findByUserIdAndYearMonth(userId, year, month);
    }

    public MonthlyBudget upsertBudget(Long userId, Long categoryId, int year, int month, BigDecimal amount) {
        MonthlyBudget existing = monthlyBudgetMapper.findByUniqueKey(userId, categoryId, year, month);
        if (existing == null) {
            MonthlyBudget budget = new MonthlyBudget();
            budget.setUserId(userId);
            budget.setCategoryId(categoryId);
            budget.setYear(year);
            budget.setMonth(month);
            budget.setAmount(amount);
            budget.setCreatedAt(LocalDateTime.now());
            budget.setUpdatedAt(LocalDateTime.now());
            monthlyBudgetMapper.insert(budget);
            return budget;
        }

        existing.setAmount(amount);
        existing.setUpdatedAt(LocalDateTime.now());
        monthlyBudgetMapper.update(existing);
        return existing;
    }

    public void deleteBudget(Long userId, Long categoryId, int year, int month) {
        monthlyBudgetMapper.deleteByUniqueKey(userId, categoryId, year, month);
    }
}

