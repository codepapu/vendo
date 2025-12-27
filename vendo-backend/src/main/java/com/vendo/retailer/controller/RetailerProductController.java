package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.retailer.service.RetailerProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/retailers/{retailerId}/products")
@RequiredArgsConstructor
public class RetailerProductController {
    
    private final RetailerProductService retailerProductService;
    
    @PostMapping
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> addProduct(
            @PathVariable Long retailerId,
            @Valid @RequestBody ProductRetailerRequest request) {
        ProductRetailerResponse productRetailer = retailerProductService.addProduct(retailerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added successfully", productRetailer));
    }
    
    @PutMapping("/{productRetailerId}/price")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> updateProductPrice(
            @PathVariable Long retailerId,
            @PathVariable Long productRetailerId,
            @RequestParam BigDecimal newPrice) {
        ProductRetailerResponse productRetailer = retailerProductService.updateProductPrice(
                retailerId, productRetailerId, newPrice);
        return ResponseEntity.ok(ApiResponse.success("Product price updated successfully", productRetailer));
    }
    
    @PutMapping("/{productRetailerId}/stock")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> updateStock(
            @PathVariable Long retailerId,
            @PathVariable Long productRetailerId,
            @RequestParam Integer newStockQuantity) {
        ProductRetailerResponse productRetailer = retailerProductService.updateStock(
                retailerId, productRetailerId, newStockQuantity);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", productRetailer));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<ProductRetailerResponse>>> getRetailerProducts(
            @PathVariable Long retailerId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ProductRetailerResponse> products = retailerProductService.getRetailerProducts(
                retailerId, isActive, searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @PutMapping("/{productRetailerId}/deactivate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> deactivateProduct(
            @PathVariable Long retailerId,
            @PathVariable Long productRetailerId) {
        ProductRetailerResponse productRetailer = retailerProductService.deactivateProduct(
                retailerId, productRetailerId);
        return ResponseEntity.ok(ApiResponse.success("Product deactivated successfully", productRetailer));
    }
    
    @PutMapping("/{productRetailerId}/activate")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> activateProduct(
            @PathVariable Long retailerId,
            @PathVariable Long productRetailerId) {
        ProductRetailerResponse productRetailer = retailerProductService.activateProduct(
                retailerId, productRetailerId);
        return ResponseEntity.ok(ApiResponse.success("Product activated successfully", productRetailer));
    }
}

