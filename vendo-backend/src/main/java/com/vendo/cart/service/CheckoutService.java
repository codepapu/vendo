package com.vendo.cart.service;

import com.vendo.cart.dto.request.CheckoutRequest;
import com.vendo.cart.dto.response.CartResponse;
import com.vendo.cart.entity.Cart;
import com.vendo.cart.entity.CartItem;
import com.vendo.cart.repository.CartRepository;
import com.vendo.cart.repository.CartItemRepository;
import com.vendo.inventory.dto.request.ReserveInventoryRequest;
import com.vendo.inventory.dto.request.ReleaseInventoryRequest;
import com.vendo.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final InventoryService inventoryService;
    
    @Transactional
    public CartResponse processCheckout(Long userId, CheckoutRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new com.vendo.common.exception.ResourceNotFoundException("Cart", "userId", userId));
        
        // Validate cart has items
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new com.vendo.common.exception.BadRequestException("Cart is empty");
        }
        
        // Reserve inventory for cart items
        // In production, would call Product Service to reserve inventory
        // For now, log the reservation step
        log.info("Reserving inventory for checkout - user: {}", userId);
        // TODO: Implement actual inventory reservation via Product Service
        // This would:
        // 1. Get cart items
        // 2. For each item, call Product Service to reserve quantity
        // 3. If any item is out of stock, throw exception and release reservations
        // 4. If successful, proceed with order creation
        
        // Process checkout
        // In production, this would:
        // 1. Validate inventory (done above)
        // 2. Calculate totals (tax, shipping, discounts)
        // 3. Create order via Order Service
        // 4. Process payment via Payment Service
        // 5. Clear cart
        // 6. Release inventory reservation on success, or keep reserved until order completion
        
        log.info("Checkout processed for user: {}", userId);
        
        // Return cart response (in production, would return order response)
        return CartResponse.builder()
                .userId(userId)
                .items(List.of())
                .totalItems(0)
                .subtotal(java.math.BigDecimal.ZERO)
                .tax(java.math.BigDecimal.ZERO)
                .shipping(java.math.BigDecimal.ZERO)
                .total(java.math.BigDecimal.ZERO)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
    
    /**
     * Release inventory reservations for cart items.
     * Called when checkout fails or is cancelled.
     */
    private void releaseInventoryReservations(Cart cart, List<ReserveInventoryRequest> reservationRequests, Long userId) {
        if (reservationRequests == null || reservationRequests.isEmpty()) {
            return;
        }
        
        log.debug("Releasing inventory reservations for cart: {}, items: {}", 
                cart.getId(), reservationRequests.size());
        
        for (ReserveInventoryRequest reserveRequest : reservationRequests) {
            try {
                ReleaseInventoryRequest releaseRequest = ReleaseInventoryRequest.builder()
                        .productId(reserveRequest.getProductId())
                        .retailerId(reserveRequest.getRetailerId())
                        .warehouseId(reserveRequest.getWarehouseId())
                        .quantity(reserveRequest.getQuantity())
                        .build();
                
                inventoryService.releaseInventory(releaseRequest, userId);
                log.debug("Inventory released for product: {}, quantity: {}", 
                        reserveRequest.getProductId(), reserveRequest.getQuantity());
            } catch (Exception e) {
                log.error("Failed to release inventory for product {}: {}", 
                        reserveRequest.getProductId(), e.getMessage(), e);
                // Continue releasing other items even if one fails
            }
        }
        
        log.info("Inventory reservations released for cart: {}", cart.getId());
    }
    
    /**
     * Release inventory reservation for a single cart.
     * Can be called when checkout is cancelled or order is cancelled.
     */
    public void releaseInventoryReservation(Cart cart, Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems == null || cartItems.isEmpty()) {
            return;
        }
        
        List<ReserveInventoryRequest> reservationRequests = new ArrayList<>();
        for (CartItem item : cartItems) {
            reservationRequests.add(ReserveInventoryRequest.builder()
                    .productId(item.getProduct().getId())
                    .retailerId(item.getRetailer() != null ? item.getRetailer().getId() : null)
                    .warehouseId(null)
                    .quantity(item.getQuantity())
                    .build());
        }
        
        releaseInventoryReservations(cart, reservationRequests, userId);
    }
}
