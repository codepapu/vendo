package com.vendo.product.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.CategoryRequest;
import com.vendo.product.dto.response.CategoryResponse;
import com.vendo.product.entity.Category;
import com.vendo.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(String status) {
        List<Category> categories;
        if (status != null && !status.isEmpty()) {
            categories = categoryRepository.findByStatusOrderByOrderAsc(status);
        } else {
            categories = categoryRepository.findAll();
        }
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findRootCategories();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryChildren(Long parentId) {
        List<Category> categories = categoryRepository.findByParentId(parentId);
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToResponse(category);
    }
    
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Validate name uniqueness
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name " + request.getName() + " already exists");
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : "published")
                .sortOrder(request.getOrder() != null ? request.getOrder() : 0)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .imageUrl(request.getImage())
                .build();
        
        // Set parent if provided
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        }
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created with ID: {}", savedCategory.getId());
        return mapToResponse(savedCategory);
    }
    
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Validate name uniqueness if changed
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new BadRequestException("Category with name " + request.getName() + " already exists");
            }
            category.setName(request.getName());
        }
        
        // Update fields
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getStatus() != null) category.setStatus(request.getStatus());
        if (request.getOrder() != null) category.setOrder(request.getOrder());
        if (request.getIsFeatured() != null) category.setIsFeatured(request.getIsFeatured());
        if (request.getImage() != null) category.setImage(request.getImage());
        
        // Update parent
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getParentId()));
            category.setParent(parent);
        } else if (request.getParentId() == null && category.getParent() != null) {
            // Remove parent if explicitly set to null
            category.setParent(null);
        }
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated with ID: {}", updatedCategory.getId());
        return mapToResponse(updatedCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        
        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new BadRequestException("Cannot delete category with children. Please delete or move children first.");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> getFeaturedCategories() {
        List<Category> categories = categoryRepository.findByIsFeaturedTrue();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(category.getChildren() != null && !category.getChildren().isEmpty() ?
                        category.getChildren().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList()) : null)
                .description(category.getDescription())
                .status(category.getStatus())
                .order(category.getOrder())
                .isFeatured(category.getIsFeatured())
                .image(category.getImage())
                .productCount(category.getProducts() != null ? (long) category.getProducts().size() : 0L)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
