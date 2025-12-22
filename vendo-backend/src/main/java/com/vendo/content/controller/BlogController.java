package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.request.BlogPostRequest;
import com.vendo.content.dto.response.BlogPostResponse;
import com.vendo.content.service.BlogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/blog/posts")
@RequiredArgsConstructor
public class BlogController {
    
    private final BlogService blogService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogPostResponse>>> getAllPosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogPostResponse> posts = blogService.getAllPosts(status, categoryId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> getPostById(@PathVariable Long id) {
        BlogPostResponse post = blogService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> getPostBySlug(@PathVariable String slug) {
        BlogPostResponse post = blogService.getPostBySlug(slug);
        blogService.incrementViewCount(post.getId());
        return ResponseEntity.ok(ApiResponse.success(post));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<BlogPostResponse>> createPost(
            @Valid @RequestBody BlogPostRequest request,
            Authentication authentication) {
        Long authorId = Long.parseLong(authentication.getName());
        BlogPostResponse post = blogService.createPost(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog post created successfully", post));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHOR')")
    public ResponseEntity<ApiResponse<BlogPostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody BlogPostRequest request) {
        BlogPostResponse post = blogService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponse.success("Blog post updated successfully", post));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        blogService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Blog post deleted successfully"));
    }
}
