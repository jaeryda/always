package com.always.service;

import com.always.entity.Category;
import com.always.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> getAllCategoriesByUserId(Long userId) {
        return categoryMapper.findAllByUserId(userId);
    }

    public List<Category> getCategoriesByUserIdAndType(Long userId, String type) {
        return categoryMapper.findByUserIdAndType(userId, type);
    }

    public Category getCategoryById(Long id) {
        return categoryMapper.findById(id);
    }

    public Category getCategoryByUserIdAndId(Long userId, Long id) {
        return categoryMapper.findByUserIdAndId(userId, id);
    }

    public Category createCategory(Category category) {
        // 중복 체크
        Category existing = categoryMapper.findByUserIdAndNameAndType(
            category.getUserId(), 
            category.getName(), 
            category.getType()
        );
        if (existing != null) {
            throw new RuntimeException("이미 존재하는 카테고리입니다: " + category.getName());
        }

        if (category.getCreatedAt() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
        if (category.getUpdatedAt() == null) {
            category.setUpdatedAt(LocalDateTime.now());
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        categoryMapper.insert(category);
        return category;
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("Category not found with id: " + id);
        }

        // 이름 변경 시 중복 체크 (자기 자신 제외)
        if (!category.getName().equals(categoryDetails.getName()) || 
            !category.getType().equals(categoryDetails.getType())) {
            Category existing = categoryMapper.findByUserIdAndNameAndType(
                category.getUserId(),
                categoryDetails.getName(),
                categoryDetails.getType()
            );
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("이미 존재하는 카테고리입니다: " + categoryDetails.getName());
            }
        }

        category.setName(categoryDetails.getName());
        category.setType(categoryDetails.getType());
        category.setIcon(categoryDetails.getIcon());
        category.setColor(categoryDetails.getColor());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setUpdatedAt(LocalDateTime.now());

        categoryMapper.update(category);
        return category;
    }

    public void deleteCategory(Long id) {
        Category category = categoryMapper.findById(id);
        if (category == null) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        categoryMapper.deleteById(id);
    }
}

