package com.nexus.content.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.content.dto.request.BlogTagRequest;
import com.nexus.content.dto.response.BlogTagResponse;
import com.nexus.content.service.BlogTagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blog/tags")
@RequiredArgsConstructor
public class BlogTagController {
    
    private final BlogTagService tagService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlogTagResponse>>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BlogTagResponse> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<BlogTagResponse>>> getAllTagsList() {
        List<BlogTagResponse> tags = tagService.getAllTagsList();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogTagResponse>> getTagById(@PathVariable Long id) {
        BlogTagResponse tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<BlogTagResponse>> getTagBySlug(@PathVariable String slug) {
        BlogTagResponse tag = tagService.getTagBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlogTagResponse>> createTag(
            @Valid @RequestBody BlogTagRequest request) {
        BlogTagResponse tag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog tag created successfully", tag));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BlogTagResponse>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody BlogTagRequest request) {
        BlogTagResponse tag = tagService.updateTag(id, request);
        return ResponseEntity.ok(ApiResponse.success("Blog tag updated successfully", tag));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.success("Blog tag deleted successfully"));
    }
}
