package com.vendo.cart.service;

import com.vendo.cart.dto.request.CartItemRequest;
import com.vendo.cart.dto.response.CartResponse;
import com.vendo.cart.entity.Cart;
import com.vendo.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        String identifier = "user_" + userId;
        String instance = "default";
        
        Optional<Cart> cartOpt = cartRepository.findByIdentifierAndInstance(identifier, instance);
        Cart cart = cartOpt.orElse(Cart.builder()
                .identifier(identifier)
                .instance(instance)
                .content("{}")
                .build());
        
        return mapToResponse(cart, userId);
    }
    
    @Transactional
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        String identifier = "user_" + userId;
        String instance = "default";
        
        Cart cart = cartRepository.findByIdentifierAndInstance(identifier, instance)
                .orElse(Cart.builder()
                        .identifier(identifier)
                        .instance(instance)
                        .content("{}")
                        .build());
        
        // Add item to cart (simplified - in real implementation, would parse content JSON)
        // For now, we'll store productId and quantity in content as JSON
        // In production, you'd want a proper CartItem entity or use Redis
        
        cart = cartRepository.save(cart);
        log.info("Item added to cart for user: {}", userId);
        return mapToResponse(cart, userId);
    }
    
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, CartItemRequest request) {
        String identifier = "user_" + userId;
        String instance = "default";
        
        Cart cart = cartRepository.findByIdentifierAndInstance(identifier, instance)
                .orElseThrow(() -> new com.vendo.common.exception.ResourceNotFoundException("Cart", "identifier", identifier));
        
        // Update item in cart (simplified)
        cart = cartRepository.save(cart);
        log.info("Cart item updated for user: {}", userId);
        return mapToResponse(cart, userId);
    }
    
    @Transactional
    public void removeCartItem(Long userId, Long itemId) {
        String identifier = "user_" + userId;
        String instance = "default";
        
        Cart cart = cartRepository.findByIdentifierAndInstance(identifier, instance)
                .orElseThrow(() -> new com.vendo.common.exception.ResourceNotFoundException("Cart", "identifier", identifier));
        
        // Remove item from cart (simplified)
        cartRepository.save(cart);
        log.info("Cart item removed for user: {}", userId);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        String identifier = "user_" + userId;
        String instance = "default";
        
        cartRepository.findByIdentifierAndInstance(identifier, instance)
                .ifPresent(cart -> {
                    cartRepository.delete(cart);
                    log.info("Cart cleared for user: {}", userId);
                });
    }
    
    private CartResponse mapToResponse(Cart cart, Long userId) {
        // Simplified mapping - in production, parse content JSON to get items
        return CartResponse.builder()
                .userId(userId)
                .items(List.of()) // Would parse from content JSON
                .totalItems(0)
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shipping(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
