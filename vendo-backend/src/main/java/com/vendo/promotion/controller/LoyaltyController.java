package com.vendo.promotion.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.promotion.dto.request.LoyaltyRequest;
import com.vendo.promotion.dto.response.LoyaltyResponse;
import com.vendo.promotion.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {
    
    private final LoyaltyService loyaltyService;
    
    @PostMapping("/enroll")
    public ResponseEntity<ApiResponse<LoyaltyResponse>> enrollInLoyalty(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        LoyaltyResponse response = loyaltyService.enrollInLoyalty(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<LoyaltyResponse>> earnPoints(
            @Valid @RequestBody LoyaltyRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        LoyaltyResponse response = loyaltyService.earnPoints(userId, request.getPurchaseAmount());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<LoyaltyResponse>> redeemPoints(
            @RequestParam BigDecimal points,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        LoyaltyResponse response = loyaltyService.redeemPoints(userId, points);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<LoyaltyResponse>> getLoyaltyStatus(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        LoyaltyResponse response = loyaltyService.getLoyaltyStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LoyaltyResponse.LoyaltyTransaction>>> getLoyaltyHistory(
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<LoyaltyResponse.LoyaltyTransaction> history = loyaltyService.getLoyaltyHistory(userId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}

