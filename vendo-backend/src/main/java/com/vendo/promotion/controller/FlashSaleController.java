package com.vendo.promotion.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.promotion.dto.request.FlashSaleRequest;
import com.vendo.promotion.dto.response.FlashSaleResponse;
import com.vendo.promotion.service.FlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions/flash-sales")
@RequiredArgsConstructor
public class FlashSaleController {
    
    private final FlashSaleService flashSaleService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> createFlashSale(
            @Valid @RequestBody FlashSaleRequest request) {
        FlashSaleResponse response = flashSaleService.createFlashSale(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> updateFlashSale(
            @PathVariable Long id,
            @Valid @RequestBody FlashSaleRequest request) {
        FlashSaleResponse response = flashSaleService.updateFlashSale(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<FlashSaleResponse>>> getActiveFlashSales() {
        List<FlashSaleResponse> flashSales = flashSaleService.getActiveFlashSales();
        return ResponseEntity.ok(ApiResponse.success(flashSales));
    }
    
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse<FlashSaleResponse>> applyFlashSaleDiscount(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        FlashSaleResponse response = flashSaleService.applyFlashSaleDiscount(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

