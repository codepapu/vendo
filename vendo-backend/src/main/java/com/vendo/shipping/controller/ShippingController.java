package com.vendo.shipping.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.shipping.dto.request.CalculateShippingRequest;
import com.vendo.shipping.dto.request.CreateShipmentLabelRequest;
import com.vendo.shipping.dto.response.ShippingResponse;
import com.vendo.shipping.dto.response.TrackingResponse;
import com.vendo.shipping.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShippingController {
    
    private final ShippingService shippingService;
    
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ShippingResponse>> calculateShipping(
            @Valid @RequestBody CalculateShippingRequest request) {
        ShippingResponse response = shippingService.calculateShipping(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<ShippingResponse.ShippingMethod>>> getShippingMethods() {
        List<ShippingResponse.ShippingMethod> methods = shippingService.getShippingMethods();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }
    
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<TrackingResponse>> trackShipment(
            @PathVariable String trackingNumber,
            @RequestParam(required = false) String carrier) {
        TrackingResponse response = shippingService.trackShipment(trackingNumber, carrier);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/label")
    public ResponseEntity<ApiResponse<ShippingResponse>> createShipmentLabel(
            @Valid @RequestBody CreateShipmentLabelRequest request) {
        ShippingResponse response = shippingService.createShipmentLabel(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

