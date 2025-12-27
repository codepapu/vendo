package com.vendo.tax.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.common.dto.ApiResponse;
import com.vendo.tax.dto.request.TaxRequest;
import com.vendo.tax.dto.response.TaxRateResponse;
import com.vendo.tax.dto.response.TaxResponse;
import com.vendo.tax.entity.TaxRule;
import com.vendo.tax.service.TaxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Tax Controller
 * Handles tax calculation and tax rate queries
 */
@RestController
@RequestMapping("/api/v1/tax")
@RequiredArgsConstructor
public class TaxController {
    
    private final TaxService taxService;
    
    /**
     * Calculate tax for an order
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<TaxResponse>> calculateTax(@Valid @RequestBody TaxRequest request) {
        TaxResponse response = taxService.calculateTax(request);
        return ResponseEntity.ok(ApiResponse.success("Tax calculated successfully", response));
    }
    
    /**
     * Get tax rates for a location
     */
    @GetMapping("/rates")
    public ResponseEntity<ApiResponse<TaxRateResponse>> getTaxRates(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String businessType) {
        TaxRateResponse response = taxService.getTaxRates(country, state, city, productType, businessType);
        return ResponseEntity.ok(ApiResponse.success("Tax rates retrieved successfully", response));
    }
    
    /**
     * Get tax by location
     */
    @GetMapping("/location")
    public ResponseEntity<ApiResponse<TaxRateResponse>> getTaxByLocation(
            @RequestParam String country,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String city) {
        TaxRateResponse response = taxService.getTaxByLocation(country, state, city);
        return ResponseEntity.ok(ApiResponse.success("Tax rates for location retrieved successfully", response));
    }
    
    /**
     * Apply tax rules
     */
    @PostMapping("/rules/apply")
    public ResponseEntity<ApiResponse<List<TaxRule>>> applyTaxRules(@Valid @RequestBody TaxRequest request) {
        List<TaxRule> rules = taxService.applyTaxRules(request);
        return ResponseEntity.ok(ApiResponse.success("Tax rules applied successfully", rules));
    }
}

