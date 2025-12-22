package com.vendo.cart.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.cart.dto.request.CartItemRequest;
import com.vendo.cart.dto.response.CartResponse;
import com.vendo.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
    
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        CartResponse cart = cartService.addItemToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }
    
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        CartResponse cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }
    
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }
    
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
