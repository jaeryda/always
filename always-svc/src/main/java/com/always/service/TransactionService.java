package com.always.service;

import com.always.entity.Transaction;
import com.always.mapper.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    private final TransactionMapper transactionMapper;

    @Autowired
    public TransactionService(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    public List<Transaction> getAllTransactionsByUserId(Long userId) {
        return transactionMapper.findAllByUserId(userId);
    }

    public List<Transaction> getTransactionsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public List<Transaction> getTransactionsByUserIdAndYearMonth(Long userId, int year, int month) {
        return transactionMapper.findByUserIdAndYearMonth(userId, year, month);
    }

    public List<Transaction> getTransactionsByUserIdAndCategoryId(Long userId, Long categoryId) {
        return transactionMapper.findByUserIdAndCategoryId(userId, categoryId);
    }

    public List<Transaction> getTransactionsByUserIdAndType(Long userId, String type) {
        return transactionMapper.findByUserIdAndType(userId, type);
    }

    public Transaction getTransactionById(Long id) {
        return transactionMapper.findById(id);
    }

    public Transaction getTransactionByUserIdAndId(Long userId, Long id) {
        return transactionMapper.findByUserIdAndId(userId, id);
    }

    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }
        if (transaction.getUpdatedAt() == null) {
            transaction.setUpdatedAt(LocalDateTime.now());
        }
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }

        transactionMapper.insert(transaction);
        return transaction;
    }

    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        Transaction transaction = transactionMapper.findById(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }

        transaction.setCategoryId(transactionDetails.getCategoryId());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setType(transactionDetails.getType());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setTransactionDate(transactionDetails.getTransactionDate());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionMapper.update(transaction);
        return transaction;
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = transactionMapper.findById(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        transactionMapper.deleteById(id);
    }

    public BigDecimal getTotalByUserIdAndTypeAndYearMonth(Long userId, String type, int year, int month) {
        BigDecimal total = transactionMapper.sumByUserIdAndTypeAndYearMonth(userId, type, year, month);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalByUserIdAndCategoryIdAndYearMonth(Long userId, Long categoryId, int year, int month) {
        BigDecimal total = transactionMapper.sumByUserIdAndCategoryIdAndYearMonth(userId, categoryId, year, month);
        return total != null ? total : BigDecimal.ZERO;
    }
}

