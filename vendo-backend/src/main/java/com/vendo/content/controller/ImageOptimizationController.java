package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.response.ImageOptimizationResponse;
import com.vendo.content.service.ImageOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/images/optimize")
@RequiredArgsConstructor
@Tag(name = "Image Optimization", description = "Image optimization and responsive image generation endpoints")
public class ImageOptimizationController {
    
    private final ImageOptimizationService imageOptimizationService;
    
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Optimize uploaded image", description = "Upload and optimize an image with multiple sizes and formats")
    public ResponseEntity<ApiResponse<ImageOptimizationResponse>> optimizeUploadedImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        Long userId = Long.parseLong(authentication.getName());
        ImageOptimizationResponse response = imageOptimizationService.optimizeImage(file, userId);
        return ResponseEntity.ok(ApiResponse.success("Image optimized successfully", response));
    }
    
    @PostMapping("/{imageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Optimize existing image", description = "Generate optimized versions of an existing image")
    public ResponseEntity<ApiResponse<ImageOptimizationResponse>> optimizeExistingImage(
            @PathVariable Long imageId
    ) throws IOException {
        ImageOptimizationResponse response = imageOptimizationService.optimizeExistingImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("Image optimized successfully", response));
    }
}

