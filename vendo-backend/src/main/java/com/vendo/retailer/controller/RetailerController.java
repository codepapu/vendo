package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.retailer.dto.request.RetailerRequest;
import com.vendo.retailer.dto.response.RetailerResponse;
import com.vendo.retailer.service.RetailerService;
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

@RestController
@RequestMapping("/api/v1/retailers")
@RequiredArgsConstructor
public class RetailerController {
    
    private final RetailerService retailerService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RetailerResponse>>> getAllRetailers(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<RetailerResponse> retailers = retailerService.getAllRetailers(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(retailers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RetailerResponse>> getRetailerById(@PathVariable Long id) {
        RetailerResponse retailer = retailerService.getRetailerById(id);
        return ResponseEntity.ok(ApiResponse.success(retailer));
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<RetailerResponse>> getRetailerBySlug(@PathVariable String slug) {
        RetailerResponse retailer = retailerService.getRetailerBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(retailer));
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerResponse>> getRetailerByUserId(@PathVariable Long userId) {
        RetailerResponse retailer = retailerService.getRetailerByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(retailer));
    }
    
    @PostMapping("/register")
    @PreAuthorize("hasRole('USER') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<RetailerResponse>> registerRetailer(
            @Valid @RequestBody RetailerRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        RetailerResponse retailer = retailerService.createRetailer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Retailer registered successfully", retailer));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RETAILER')")
    public ResponseEntity<ApiResponse<RetailerResponse>> updateRetailer(
            @PathVariable Long id,
            @Valid @RequestBody RetailerRequest request) {
        RetailerResponse retailer = retailerService.updateRetailer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Retailer updated successfully", retailer));
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetailerResponse>> updateRetailerStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        RetailerResponse retailer = retailerService.updateRetailerStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Retailer status updated successfully", retailer));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRetailer(@PathVariable Long id) {
        retailerService.deleteRetailer(id);
        return ResponseEntity.ok(ApiResponse.success("Retailer deleted successfully"));
    }
}

