package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.response.PayoutResponse;
import com.vendo.retailer.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/retailers/{retailerId}/payouts")
@RequiredArgsConstructor
public class PayoutController {
    
    private final PayoutService payoutService;
    
    /**
     * Calculate payout for a period
     */
    @GetMapping("/calculate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> calculatePayout(
            @PathVariable Long retailerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        PayoutResponse payout = payoutService.calculatePayout(retailerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(payout));
    }
    
    /**
     * Process payout to retailer bank account
     */
    @PostMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PayoutResponse>> processPayout(
            @PathVariable Long retailerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long bankAccountId,
            Authentication authentication) {
        Long processedBy = Long.parseLong(authentication.getName());
        PayoutResponse payout = payoutService.processPayout(retailerId, startDate, endDate, bankAccountId, processedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payout processed successfully", payout));
    }
    
    /**
     * Get payout history for a retailer
     */
    @GetMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getPayoutHistory(
            @PathVariable Long retailerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<PayoutResponse> payouts = payoutService.getPayoutHistory(retailerId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
    
    /**
     * Get pending payouts for a retailer
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PayoutResponse>>> getPendingPayouts(
            @PathVariable Long retailerId) {
        List<PayoutResponse> payouts = payoutService.getPendingPayouts(retailerId);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
    
    /**
     * Get all pending payouts (admin only)
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PayoutResponse>>> getAllPendingPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<PayoutResponse> payouts = payoutService.getAllPendingPayouts(pageable);
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }
}

