package com.vendo.promotion.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.promotion.dto.response.ReferralResponse;
import com.vendo.promotion.service.ReferralService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
public class ReferralController {
    
    private final ReferralService referralService;
    
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ReferralResponse>> generateReferralCode(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ReferralResponse response = referralService.generateReferralCode(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<ReferralResponse>> trackReferral(
            @RequestParam String referralCode,
            Authentication authentication) {
        Long refereeUserId = Long.parseLong(authentication.getName());
        ReferralResponse response = referralService.trackReferral(referralCode, refereeUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/process-reward")
    public ResponseEntity<ApiResponse<ReferralResponse>> processReferralReward(
            @RequestParam String referralCode,
            @RequestParam BigDecimal orderAmount,
            Authentication authentication) {
        Long refereeUserId = Long.parseLong(authentication.getName());
        ReferralResponse response = referralService.processReferralReward(referralCode, refereeUserId, orderAmount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReferralResponse>> getReferralStats(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        ReferralResponse response = referralService.getReferralStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

