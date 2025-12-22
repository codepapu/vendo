package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.request.BlogCommentRequest;
import com.vendo.content.dto.response.BlogCommentResponse;
import com.vendo.content.service.BlogCommentService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/blog/comments")
@RequiredArgsConstructor
public class BlogCommentController {
    
    private final BlogCommentService commentService;
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<BlogCommentResponse>>> getCommentsByPostId(@PathVariable Long postId) {
        List<BlogCommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<BlogCommentResponse>>> getPendingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogCommentResponse> comments = commentService.getPendingComments(pageable);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<BlogCommentResponse>> createComment(
            @Valid @RequestBody BlogCommentRequest request,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        BlogCommentResponse comment = commentService.createComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment submitted successfully", comment));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlogCommentResponse>> updateCommentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        BlogCommentResponse comment = commentService.updateCommentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Comment status updated", comment));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }
}
