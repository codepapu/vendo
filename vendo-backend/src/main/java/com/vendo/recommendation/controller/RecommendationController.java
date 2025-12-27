package com.vendo.recommendation.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.response.ProductResponse;
import com.vendo.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendedProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = Long.parseLong(authentication.getName());
        List<ProductResponse> products = recommendationService.getRecommendedProducts(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/similar/{productId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<ProductResponse> products = recommendationService.getSimilarProducts(productId, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/recently-viewed")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecentlyViewed(
            Authentication authentication,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = Long.parseLong(authentication.getName());
        List<ProductResponse> products = recommendationService.getRecentlyViewed(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<Void>> trackUserBehavior(
            Authentication authentication,
            @RequestParam Long productId,
            @RequestParam String action) {
        Long userId = Long.parseLong(authentication.getName());
        recommendationService.trackUserBehavior(userId, productId, action);
        return ResponseEntity.ok(ApiResponse.success("Behavior tracked"));
    }
}

