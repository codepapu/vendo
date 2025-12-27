package com.vendo.inventory.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.inventory.dto.response.LowStockAlertResponse;
import com.vendo.inventory.service.LowStockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/low-stock-alerts")
@RequiredArgsConstructor
public class LowStockAlertController {
    
    private final LowStockAlertService lowStockAlertService;
    
    @GetMapping("/check")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<LowStockAlertResponse>> checkLowStock(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId) {
        LowStockAlertResponse response = lowStockAlertService.checkLowStock(retailerId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendLowStockAlerts(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId) {
        lowStockAlertService.sendLowStockAlerts(retailerId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success("Low stock alerts sent"));
    }
    
    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<LowStockAlertResponse.LowStockProduct>>> getLowStockProducts(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId,
            Pageable pageable) {
        List<LowStockAlertResponse.LowStockProduct> products = 
                lowStockAlertService.getLowStockProducts(retailerId, warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @PutMapping("/thresholds/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> updateAlertThresholds(
            @PathVariable Long inventoryId,
            @RequestParam(required = false) Integer lowStockThreshold,
            @RequestParam(required = false) Integer reorderPoint) {
        lowStockAlertService.updateAlertThresholds(inventoryId, lowStockThreshold, reorderPoint);
        return ResponseEntity.ok(ApiResponse.success("Alert thresholds updated"));
    }
}

