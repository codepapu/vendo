package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.response.ContentResponse;
import com.vendo.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class ContentController {
    
    private final ContentService contentService;
    
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentOverview(
            @RequestParam(required = false) Integer blogLimit,
            @RequestParam(required = false) Integer pageLimit,
            @RequestParam(required = false) Integer mediaLimit) {
        ContentResponse content = contentService.getContentOverview(blogLimit, pageLimit, mediaLimit);
        return ResponseEntity.ok(ApiResponse.success(content));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentByType(
            @PathVariable String type,
            @RequestParam(required = false) Integer limit) {
        ContentResponse content = contentService.getContentByType(type, limit);
        return ResponseEntity.ok(ApiResponse.success(content));
    }
}
