package com.vendo.inventory.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.inventory.dto.request.StockAdjustmentRequest;
import com.vendo.inventory.dto.response.StockAdjustmentResponse;
import com.vendo.inventory.service.StockAdjustmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/adjustments")
@RequiredArgsConstructor
public class StockAdjustmentController {
    
    private final StockAdjustmentService stockAdjustmentService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        StockAdjustmentResponse response = stockAdjustmentService.adjustStock(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> approveAdjustment(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        Long approverId = Long.parseLong(authentication.getName());
        StockAdjustmentResponse response = stockAdjustmentService.approveAdjustment(id, approverId, notes);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StockAdjustmentResponse>> rejectAdjustment(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {
        Long approverId = Long.parseLong(authentication.getName());
        StockAdjustmentResponse response = stockAdjustmentService.rejectAdjustment(id, approverId, reason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/history/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<StockAdjustmentResponse>>> getAdjustmentHistory(
            @PathVariable Long inventoryId) {
        List<StockAdjustmentResponse> history = stockAdjustmentService.getAdjustmentHistory(inventoryId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<StockAdjustmentResponse>>> getAdjustmentsByStatus(
            @PathVariable String status,
            Pageable pageable) {
        Page<StockAdjustmentResponse> adjustments = stockAdjustmentService.getAdjustmentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(adjustments));
    }
    
    @GetMapping("/pending-approvals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StockAdjustmentResponse>>> getPendingApprovals() {
        List<StockAdjustmentResponse> adjustments = stockAdjustmentService.getPendingApprovals();
        return ResponseEntity.ok(ApiResponse.success(adjustments));
    }
}

