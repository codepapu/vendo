package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.request.BlogCategoryRequest;
import com.vendo.content.dto.response.BlogCategoryResponse;
import com.vendo.content.service.BlogCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blog/categories")
@RequiredArgsConstructor
public class BlogCategoryController {
    
    private final BlogCategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<BlogCategoryResponse>>> getAllCategories() {
        List<BlogCategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    @GetMapping("/root")
    public ResponseEntity<ApiResponse<List<BlogCategoryResponse>>> getRootCategories() {
        List<BlogCategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogCategoryResponse>> getCategoryById(@PathVariable Long id) {
        BlogCategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlogCategoryResponse>> createCategory(
            @Valid @RequestBody BlogCategoryRequest request) {
        BlogCategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog category created successfully", category));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlogCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody BlogCategoryRequest request) {
        BlogCategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Blog category updated successfully", category));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Blog category deleted successfully"));
    }
}
