package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.ApplyCouponRequest;
import com.vendo.product.dto.request.CouponRequest;
import com.vendo.product.dto.response.CouponResponse;
import com.vendo.product.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for managing coupons and discounts.
 * Provides endpoints to validate, apply, retrieve, and create coupons.
 */
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    /**
     * Validate a coupon code.
     * Checks if the coupon is valid, not expired, and meets all requirements.
     * 
     * @param code Coupon code to validate
     * @param orderAmount Optional order amount for minimum purchase validation
     * @param authentication Authentication object containing user information
     * @return CouponResponse with validation details
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<CouponResponse>> validateCoupon(
            @RequestParam String code,
            @RequestParam(required = false) BigDecimal orderAmount,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        CouponResponse response = couponService.validateCoupon(code, userId, orderAmount);
        return ResponseEntity.ok(ApiResponse.success("Coupon validation result", response));
    }
    
    /**
     * Apply a coupon to an order and calculate discount.
     * 
     * @param request ApplyCouponRequest with coupon code, order amount, and optional product/category IDs
     * @param authentication Authentication object containing user information
     * @return CouponResponse with calculated discount details
     */
    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CouponResponse>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        CouponResponse response = couponService.applyCoupon(
                request.getCode(), 
                request.getOrderAmount(), 
                userId, 
                request.getProductIds(), 
                request.getCategoryIds());
        return ResponseEntity.ok(ApiResponse.success("Coupon applied successfully", response));
    }
    
    /**
     * Get all available coupons for the authenticated user.
     * Returns active, non-expired coupons applicable to the user.
     * 
     * @param authentication Authentication object containing user information
     * @return List of available CouponResponse
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons(
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        List<CouponResponse> coupons = couponService.getAvailableCoupons(userId);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }
    
    /**
     * Get all coupons (admin only).
     * 
     * @return List of all CouponResponse
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        List<CouponResponse> coupons = couponService.getAvailableCoupons(null);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }
    
    /**
     * Get a coupon by ID (admin only).
     * 
     * @param id Coupon ID
     * @return CouponResponse
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponById(@PathVariable Long id) {
        CouponResponse response = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get a coupon by code.
     * 
     * @param code Coupon code
     * @return CouponResponse
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
        CouponResponse response = couponService.validateCoupon(code, null, null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Create a new coupon (admin only).
     * 
     * @param request CouponRequest with coupon details
     * @return CouponResponse with created coupon details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", response));
    }
    
    /**
     * Update a coupon (admin only).
     * 
     * @param id Coupon ID
     * @param request CouponRequest with updated coupon details
     * @return CouponResponse with updated coupon details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success("Coupon updated successfully", response));
    }
    
    /**
     * Delete a coupon (admin only).
     * 
     * @param id Coupon ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted successfully"));
    }
}
