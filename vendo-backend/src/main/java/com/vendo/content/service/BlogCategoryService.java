package com.vendo.content.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.content.dto.request.BlogCategoryRequest;
import com.vendo.content.dto.response.BlogCategoryResponse;
import com.vendo.content.entity.BlogCategory;
import com.vendo.content.repository.BlogCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogCategoryService {
    
    private final BlogCategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    public List<BlogCategoryResponse> getAllCategories() {
        List<BlogCategory> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BlogCategoryResponse> getRootCategories() {
        List<BlogCategory> categories = categoryRepository.findByParentIdIsNull();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BlogCategoryResponse getCategoryById(Long id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category", "id", id));
        return mapToResponse(category);
    }
    
    @Transactional
    public BlogCategoryResponse createCategory(BlogCategoryRequest request) {
        // Check if slug already exists
        if (request.getSlug() != null && categoryRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
        }
        
        BlogCategory category = BlogCategory.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .parentId(request.getParentId())
                .image(request.getImage())
                .postCount(0L)
                .build();
        
        BlogCategory savedCategory = categoryRepository.save(category);
        log.info("Blog category created: ID={}, Name={}", savedCategory.getId(), savedCategory.getName());
        
        return mapToResponse(savedCategory);
    }
    
    @Transactional
    public BlogCategoryResponse updateCategory(Long id, BlogCategoryRequest request) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category", "id", id));
        
        // Check slug uniqueness if changed
        if (request.getSlug() != null && !category.getSlug().equals(request.getSlug())) {
            if (categoryRepository.findBySlug(request.getSlug()).isPresent()) {
                throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
            }
        }
        
        category.setName(request.getName());
        if (request.getSlug() != null) {
            category.setSlug(request.getSlug());
        }
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());
        category.setImage(request.getImage());
        
        BlogCategory updatedCategory = categoryRepository.save(category);
        log.info("Blog category updated: ID={}", id);
        
        return mapToResponse(updatedCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog category", "id", id));
        
        // Check if category has children
        List<BlogCategory> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new BadRequestException("Cannot delete category with child categories");
        }
        
        categoryRepository.delete(category);
        log.info("Blog category deleted: ID={}", id);
    }
    
    private BlogCategoryResponse mapToResponse(BlogCategory category) {
        List<BlogCategory> children = categoryRepository.findByParentId(category.getId());
        List<BlogCategoryResponse> childrenResponses = children.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return BlogCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .image(category.getImage())
                .postCount(category.getPostCount())
                .children(childrenResponses.isEmpty() ? null : childrenResponses)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
