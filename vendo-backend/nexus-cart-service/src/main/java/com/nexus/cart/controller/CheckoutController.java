package com.nexus.cart.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.cart.dto.request.CheckoutRequest;
import com.nexus.cart.dto.response.CartResponse;
import com.nexus.cart.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    
    private final CheckoutService checkoutService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        CartResponse result = checkoutService.processCheckout(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Checkout processed successfully", result));
    }
}
