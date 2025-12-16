package com.nexus.marketplace.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.marketplace.dto.request.VendorRegistrationRequest;
import com.nexus.marketplace.dto.response.VendorResponse;
import com.nexus.marketplace.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {
    
    private final VendorService vendorService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<VendorResponse>> registerVendor(
            @Valid @RequestBody VendorRegistrationRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        request.setUserId(userId);
        VendorResponse vendor = vendorService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor registration submitted", vendor));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<VendorResponse>>> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<VendorResponse> vendors = vendorService.getAllVendors(pageable);
        return ResponseEntity.ok(ApiResponse.success(vendors));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(@PathVariable Long id) {
        VendorResponse vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }
    
    @GetMapping("/{id}/products")
    public ResponseEntity<ApiResponse<List<VendorResponse>>> getVendorProducts(@PathVariable Long id) {
        List<VendorResponse> products = vendorService.getVendorProducts(id);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @GetMapping("/{id}/commissions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorCommissions(@PathVariable Long id) {
        VendorResponse vendor = vendorService.getVendorCommissions(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }
    
    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorAnalytics(@PathVariable Long id) {
        VendorResponse vendor = vendorService.getVendorAnalytics(id);
        return ResponseEntity.ok(ApiResponse.success(vendor));
    }
}
