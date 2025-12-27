package com.vendo.order.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.order.dto.request.SelectShippingMethodRequest;
import com.vendo.order.dto.request.ShippingEstimateRequest;
import com.vendo.order.dto.response.ShippingEstimateResponse;
import com.vendo.order.dto.response.ShippingMethodResponse;
import com.vendo.order.service.ShippingMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping-methods")
@RequiredArgsConstructor
public class ShippingMethodController {
    
    private final ShippingMethodService shippingMethodService;
    
    /**
     * Get all available shipping methods
     * GET /api/v1/shipping-methods
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShippingMethodResponse>>> getAvailableShippingMethods() {
        List<ShippingMethodResponse> methods = shippingMethodService.getAvailableShippingMethods();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }
    
    /**
     * Get available shipping methods based on address and weight
     * GET /api/v1/shipping-methods/available
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<ShippingMethodResponse>>> getAvailableShippingMethods(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) BigDecimal weight) {
        
        // If address params provided, create address info
        ShippingEstimateRequest.AddressInfo address = null;
        if (city != null && state != null && postalCode != null && country != null) {
            address = ShippingEstimateRequest.AddressInfo.builder()
                    .city(city)
                    .state(state)
                    .postalCode(postalCode)
                    .country(country)
                    .build();
        }
        
        // If address and weight provided, return methods with calculated costs
        if (address != null && weight != null) {
            List<ShippingMethodResponse> methods = shippingMethodService.getAvailableShippingMethods(address, weight);
            return ResponseEntity.ok(ApiResponse.success(methods));
        }
        
        // Otherwise, return all available methods
        List<ShippingMethodResponse> methods = shippingMethodService.getAvailableShippingMethods();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }
    
    /**
     * Calculate shipping cost for a specific method and weight
     * GET /api/v1/shipping-methods/{id}/calculate?weight={weight}
     * GET /api/v1/shipping-methods/calculate?code={code}&weight={weight}
     */
    @GetMapping("/{id}/calculate")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateShippingCost(
            @PathVariable Long id,
            @RequestParam BigDecimal weight) {
        BigDecimal cost = shippingMethodService.calculateShippingCost(id, weight);
        return ResponseEntity.ok(ApiResponse.success(cost));
    }
    
    @GetMapping("/calculate")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateShippingCostByCode(
            @RequestParam String code,
            @RequestParam BigDecimal weight) {
        BigDecimal cost = shippingMethodService.calculateShippingCost(code, weight);
        return ResponseEntity.ok(ApiResponse.success(cost));
    }
    
    /**
     * Select shipping method (returns method with calculated cost)
     * POST /api/v1/shipping-methods/select
     */
    @PostMapping("/select")
    public ResponseEntity<ApiResponse<ShippingMethodResponse>> selectShippingMethod(
            @Valid @RequestBody SelectShippingMethodRequest request,
            @RequestParam(required = false) BigDecimal weight) {
        
        BigDecimal totalWeight = weight != null ? weight : BigDecimal.ZERO;
        ShippingMethodResponse method = shippingMethodService.selectShippingMethod(request, totalWeight);
        return ResponseEntity.ok(ApiResponse.success("Shipping method selected", method));
    }
    
    /**
     * Get shipping estimates for address and weight
     * Supports single address or multiple addresses per order
     * POST /api/v1/shipping-methods/estimates
     */
    @PostMapping("/estimates")
    public ResponseEntity<ApiResponse<ShippingEstimateResponse>> getShippingEstimates(
            @Valid @RequestBody ShippingEstimateRequest request) {
        ShippingEstimateResponse estimates = shippingMethodService.getShippingEstimates(request);
        return ResponseEntity.ok(ApiResponse.success(estimates));
    }
    
    /**
     * Get shipping estimates with query parameters (simpler API)
     * GET /api/v1/shipping-methods/estimates?city={city}&state={state}&postalCode={postalCode}&country={country}&weight={weight}
     */
    @GetMapping("/estimates")
    public ResponseEntity<ApiResponse<ShippingEstimateResponse>> getShippingEstimatesSimple(
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String postalCode,
            @RequestParam String country,
            @RequestParam BigDecimal weight) {
        
        ShippingEstimateRequest request = ShippingEstimateRequest.builder()
                .address(ShippingEstimateRequest.AddressInfo.builder()
                        .city(city)
                        .state(state)
                        .postalCode(postalCode)
                        .country(country)
                        .build())
                .totalWeight(weight)
                .build();
        
        ShippingEstimateResponse estimates = shippingMethodService.getShippingEstimates(request);
        return ResponseEntity.ok(ApiResponse.success(estimates));
    }
}

