package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.ProductRetailerRequest;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.product.service.ProductRetailerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product-retailers")
@RequiredArgsConstructor
public class ProductRetailerController {
    
    private final ProductRetailerService productRetailerService;
    
    @GetMapping("/retailer/{retailerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Page<ProductRetailerResponse>>> getProductsByRetailer(
            @PathVariable Long retailerId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductRetailerResponse> products = productRetailerService.getProductsByRetailer(retailerId, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductRetailerResponse>>> getRetailersByProduct(
            @PathVariable Long productId) {
        List<ProductRetailerResponse> retailers = productRetailerService.getRetailersByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(retailers));
    }
    
    @GetMapping("/retailer/{retailerId}/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<List<ProductRetailerResponse>>> getLowStockProducts(
            @PathVariable Long retailerId) {
        List<ProductRetailerResponse> products = productRetailerService.getLowStockProducts(retailerId);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> getProductRetailerById(@PathVariable Long id) {
        ProductRetailerResponse productRetailer = productRetailerService.getProductRetailerById(id);
        return ResponseEntity.ok(ApiResponse.success(productRetailer));
    }
    
    @GetMapping("/product/{productId}/retailer/{retailerId}")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> getProductRetailerByProductAndRetailer(
            @PathVariable Long productId,
            @PathVariable Long retailerId) {
        ProductRetailerResponse productRetailer = productRetailerService.getProductRetailerByProductAndRetailer(productId, retailerId);
        return ResponseEntity.ok(ApiResponse.success(productRetailer));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> createProductRetailer(
            @Valid @RequestBody ProductRetailerRequest request) {
        ProductRetailerResponse productRetailer = productRetailerService.createProductRetailer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product retailer created successfully", productRetailer));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<ProductRetailerResponse>> updateProductRetailer(
            @PathVariable Long id,
            @Valid @RequestBody ProductRetailerRequest request) {
        ProductRetailerResponse productRetailer = productRetailerService.updateProductRetailer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product retailer updated successfully", productRetailer));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<Void>> deleteProductRetailer(@PathVariable Long id) {
        productRetailerService.deleteProductRetailer(id);
        return ResponseEntity.ok(ApiResponse.success("Product retailer deleted successfully"));
    }
}

