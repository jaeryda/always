package com.always.controller;

import com.always.entity.Category;
import com.always.entity.Transaction;
import com.always.service.CategoryService;
import com.always.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account-book")
@CrossOrigin(origins = {"http://localhost:8088", "http://192.168.75.80:8088"})
public class AccountBookController {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @Autowired
    public AccountBookController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    // ========== 카테고리 관련 API ==========

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Category> categories = categoryService.getAllCategoriesByUserId(userId);
            response.put("success", true);
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/categories/{type}")
    public ResponseEntity<Map<String, Object>> getCategoriesByType(
            @PathVariable String type,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Category> categories = categoryService.getCategoriesByUserIdAndType(userId, type.toUpperCase());
            response.put("success", true);
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestBody Category category,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            category.setUserId(userId);
            Category createdCategory = categoryService.createCategory(category);
            response.put("success", true);
            response.put("message", "카테고리가 생성되었습니다.");
            response.put("category", createdCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @RequestBody Category category,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 소유권 확인
            Category existing = categoryService.getCategoryByUserIdAndId(userId, id);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "카테고리를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Category updatedCategory = categoryService.updateCategory(id, category);
            response.put("success", true);
            response.put("message", "카테고리가 수정되었습니다.");
            response.put("category", updatedCategory);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 소유권 확인
            Category existing = categoryService.getCategoryByUserIdAndId(userId, id);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "카테고리를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            categoryService.deleteCategory(id);
            response.put("success", true);
            response.put("message", "카테고리가 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== 거래 내역 관련 API ==========

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getAllTransactions(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Transaction> transactions;
            if (year != null && month != null) {
                transactions = transactionService.getTransactionsByUserIdAndYearMonth(userId, year, month);
            } else {
                transactions = transactionService.getAllTransactionsByUserId(userId);
            }

            response.put("success", true);
            response.put("transactions", transactions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "거래 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<Map<String, Object>> getTransactionById(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Transaction transaction = transactionService.getTransactionByUserIdAndId(userId, id);
            if (transaction == null) {
                response.put("success", false);
                response.put("message", "거래 내역을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("transaction", transaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "거래 내역 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> createTransaction(
            @RequestBody Transaction transaction,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            transaction.setUserId(userId);
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            response.put("success", true);
            response.put("message", "거래 내역이 추가되었습니다.");
            response.put("transaction", createdTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "거래 내역 추가 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<Map<String, Object>> updateTransaction(
            @PathVariable Long id,
            @RequestBody Transaction transaction,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 소유권 확인
            Transaction existing = transactionService.getTransactionByUserIdAndId(userId, id);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "거래 내역을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
            response.put("success", true);
            response.put("message", "거래 내역이 수정되었습니다.");
            response.put("transaction", updatedTransaction);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "거래 내역 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(
            @PathVariable Long id,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 소유권 확인
            Transaction existing = transactionService.getTransactionByUserIdAndId(userId, id);
            if (existing == null) {
                response.put("success", false);
                response.put("message", "거래 내역을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            transactionService.deleteTransaction(id);
            response.put("success", true);
            response.put("message", "거래 내역이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "거래 내역 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== 통계 관련 API ==========

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false, defaultValue = "0") Integer year,
            @RequestParam(required = false, defaultValue = "0") Integer month,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "인증 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // 현재 날짜로 기본값 설정
            if (year == 0 || month == 0) {
                LocalDate now = LocalDate.now();
                year = now.getYear();
                month = now.getMonthValue();
            }

            BigDecimal income = transactionService.getTotalByUserIdAndTypeAndYearMonth(userId, "INCOME", year, month);
            BigDecimal expense = transactionService.getTotalByUserIdAndTypeAndYearMonth(userId, "EXPENSE", year, month);
            BigDecimal balance = income.subtract(expense);

            response.put("success", true);
            response.put("year", year);
            response.put("month", month);
            response.put("income", income);
            response.put("expense", expense);
            response.put("balance", balance);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "통계 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

