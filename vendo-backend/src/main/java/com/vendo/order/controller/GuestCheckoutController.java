package com.vendo.order.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.order.dto.request.GuestOrderRequest;
import com.vendo.order.dto.response.GuestOrderResponse;
import com.vendo.order.service.GuestCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for guest checkout operations (non-authenticated users)
 */
@RestController
@RequestMapping("/api/v1/guest/checkout")
@RequiredArgsConstructor
public class GuestCheckoutController {
    
    private final GuestCheckoutService guestCheckoutService;
    
    /**
     * Process guest checkout
     * 
     * @param request Guest order request
     * @return GuestOrderResponse with order details and session token
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GuestOrderResponse>> processGuestCheckout(
            @Valid @RequestBody GuestOrderRequest request) {
        
        GuestOrderResponse response = guestCheckoutService.processGuestCheckout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Guest order created successfully", response));
    }
    
    /**
     * Get guest order by session token
     * 
     * @param sessionToken Session token from guest order
     * @return OrderResponse with order details
     */
    @GetMapping("/order")
    public ResponseEntity<ApiResponse<com.vendo.order.dto.response.OrderResponse>> getGuestOrder(
            @RequestParam String sessionToken) {
        
        com.vendo.order.dto.response.OrderResponse order = 
            guestCheckoutService.getGuestOrderByToken(sessionToken);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
}
