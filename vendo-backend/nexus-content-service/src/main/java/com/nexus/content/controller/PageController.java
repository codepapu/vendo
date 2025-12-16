package com.nexus.content.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.content.dto.request.PageRequest;
import com.nexus.content.dto.response.PageResponse;
import com.nexus.content.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {
    
    private final PageService pageService;
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PageResponse>> getPageBySlug(@PathVariable String slug) {
        PageResponse page = pageService.getPageBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PageResponse>> getPageById(@PathVariable Long id) {
        PageResponse page = pageService.getPageById(id);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse>> createPage(
            @Valid @RequestBody PageRequest request) {
        PageResponse page = pageService.createPage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Page created successfully", page));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse>> updatePage(
            @PathVariable Long id,
            @Valid @RequestBody PageRequest request) {
        PageResponse page = pageService.updatePage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Page updated successfully", page));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePage(@PathVariable Long id) {
        pageService.deletePage(id);
        return ResponseEntity.ok(ApiResponse.success("Page deleted successfully"));
    }
}
