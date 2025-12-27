package com.vendo.cart.service;

import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.b2b.service.B2BPricingService;
import com.vendo.cart.dto.request.CartItemRequest;
import com.vendo.cart.dto.response.CartPricingResponse;
import com.vendo.cart.dto.response.CartResponse;
import com.vendo.cart.entity.Cart;
import com.vendo.cart.entity.CartItem;
import com.vendo.cart.repository.CartItemRepository;
import com.vendo.cart.repository.CartRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.service.ShippingMethodService;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final B2BPricingService b2BPricingService;
    private final ShippingMethodService shippingMethodService;
    
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId, null, "B2C"));
        
        return mapToResponse(cart);
    }
    
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId, Long businessAccountId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId, businessAccountId, "B2B"));
        
        // Update cart type if business account is provided
        if (businessAccountId != null && !"B2B".equals(cart.getCartType())) {
            cart.setBusinessAccountId(businessAccountId);
            cart.setCartType("B2B");
            cart = cartRepository.save(cart);
        }
        
        return mapToResponse(cart);
    }
    
    @Transactional
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        // Get or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId, null, determineCartType(userId, request.getRetailerId())));
        
        // Determine cart type based on items
        String cartType = determineCartType(cart, request.getRetailerId());
        cart.setCartType(cartType);
        
        // Get retailer if provided
        Retailer retailer = null;
        if (request.getRetailerId() != null) {
            retailer = retailerRepository.findById(request.getRetailerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", request.getRetailerId()));
        }
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductIdAndRetailerId(
                cart.getId(), request.getProductId(), request.getRetailerId());
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            // Create new cart item
            BigDecimal unitPrice = product.getPrice();
            if (product.getSalePrice() != null && product.getSalePrice().compareTo(BigDecimal.ZERO) > 0) {
                unitPrice = product.getSalePrice();
            }
            
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .retailer(retailer)
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .build();
            
            cartItemRepository.save(cartItem);
        }
        
        cart = cartRepository.save(cart);
        log.info("Item added to cart for user: {}", userId);
        return mapToResponse(cart);
    }
    
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, CartItemRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        
        // Verify cart belongs to user
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to user");
        }
        
        if (request.getQuantity() != null && request.getQuantity() > 0) {
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }
        
        Cart cart = cartRepository.findById(item.getCart().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", item.getCart().getId()));
        
        log.info("Cart item updated for user: {}", userId);
        return mapToResponse(cart);
    }
    
    @Transactional
    public void removeCartItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));
        
        // Verify cart belongs to user
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to user");
        }
        
        cartItemRepository.delete(item);
        log.info("Cart item removed for user: {}", userId);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);
        
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cartRepository.delete(cart);
            log.info("Cart cleared for user: {}", userId);
        }
    }
    
    /**
     * Get cart pricing with B2B pricing applied if applicable
     */
    @Transactional(readOnly = true)
    public CartPricingResponse getCartPricing(Long userId, Long businessAccountId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));
        
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            return CartPricingResponse.builder()
                    .cartId(cart.getId())
                    .cartType(cart.getCartType())
                    .items(Collections.emptyList())
                    .retailerShipping(Collections.emptyMap())
                    .subtotal(BigDecimal.ZERO)
                    .tax(BigDecimal.ZERO)
                    .shipping(BigDecimal.ZERO)
                    .total(BigDecimal.ZERO)
                    .build();
        }
        
        BusinessAccount businessAccount = null;
        if (businessAccountId != null) {
            businessAccount = businessAccountRepository.findById(businessAccountId)
                    .orElse(null);
        }
        
        // Apply B2B pricing if applicable
        boolean isB2B = "B2B".equals(cart.getCartType()) && businessAccount != null;
        
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartPricingResponse.CartItemPricing> itemPricings = new ArrayList<>();
        Map<Long, BigDecimal> retailerSubtotals = new HashMap<>();
        
        for (CartItem item : items) {
            BigDecimal unitPrice = item.getUnitPrice();
            BigDecimal itemTotal;
            
            // Apply B2B pricing if applicable
            if (isB2B) {
                try {
                    var pricing = b2BPricingService.getB2BPricing(
                            item.getProduct().getId(),
                            businessAccount.getId(),
                            item.getQuantity());
                    unitPrice = pricing.getUnitPrice();
                    itemTotal = pricing.getTotalPrice();
                } catch (Exception e) {
                    log.warn("Failed to get B2B pricing for product {}: {}", 
                            item.getProduct().getId(), e.getMessage());
                    itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                }
            } else {
                itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            }
            
            subtotal = subtotal.add(itemTotal);
            
            // Track retailer subtotals for multi-vendor
            Long retailerId = item.getRetailer() != null ? item.getRetailer().getId() : null;
            if (retailerId != null) {
                retailerSubtotals.merge(retailerId, itemTotal, BigDecimal::add);
            }
            
            itemPricings.add(CartPricingResponse.CartItemPricing.builder()
                    .itemId(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .retailerId(retailerId)
                    .retailerName(item.getRetailer() != null ? item.getRetailer().getName() : null)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .itemTotal(itemTotal)
                    .isB2BPricing(isB2B)
                    .build());
        }
        
        // Calculate shipping per retailer for multi-vendor carts
        Map<Long, BigDecimal> retailerShipping = new HashMap<>();
        if ("MULTI_VENDOR".equals(cart.getCartType())) {
            List<Long> retailerIds = cartItemRepository.findDistinctRetailerIdsByCartId(cart.getId());
            for (Long retailerId : retailerIds) {
                List<CartItem> retailerItems = cartItemRepository.findByCartIdAndRetailerId(cart.getId(), retailerId);
                BigDecimal retailerWeight = calculateItemsWeight(retailerItems);
                // Use default shipping method for calculation
                try {
                    BigDecimal shippingCost = shippingMethodService.calculateShippingCost("STANDARD", retailerWeight);
                    retailerShipping.put(retailerId, shippingCost);
                } catch (Exception e) {
                    log.warn("Failed to calculate shipping for retailer {}: {}", retailerId, e.getMessage());
                    retailerShipping.put(retailerId, BigDecimal.valueOf(10)); // Default
                }
            }
        }
        
        BigDecimal shipping = retailerShipping.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate tax (10% default)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal total = subtotal.add(tax).add(shipping);
        
        return CartPricingResponse.builder()
                .cartId(cart.getId())
                .cartType(cart.getCartType())
                .items(itemPricings)
                .retailerShipping(retailerShipping)
                .retailerSubtotals(retailerSubtotals)
                .subtotal(subtotal)
                .tax(tax)
                .shipping(shipping)
                .total(total)
                .build();
    }
    
    private Cart createEmptyCart(Long userId, Long businessAccountId, String cartType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Cart cart = Cart.builder()
                .user(user)
                .businessAccountId(businessAccountId)
                .cartType(cartType)
                .currency("USD")
                .build();
        
        return cartRepository.save(cart);
    }
    
    private String determineCartType(Long userId, Long retailerId) {
        Cart existingCart = cartRepository.findByUserId(userId).orElse(null);
        return determineCartType(existingCart, retailerId);
    }
    
    private String determineCartType(Cart cart, Long retailerId) {
        if (cart == null) {
            return retailerId != null ? "MULTI_VENDOR" : "B2C";
        }
        
        // If cart already has items from different retailers, it's multi-vendor
        List<Long> retailerIds = cartItemRepository.findDistinctRetailerIdsByCartId(cart.getId());
        if (retailerIds.size() > 1 || (retailerIds.size() == 1 && retailerId != null && !retailerIds.contains(retailerId))) {
            return "MULTI_VENDOR";
        }
        
        // If adding item with retailer to B2C cart, make it multi-vendor
        if (retailerId != null && "B2C".equals(cart.getCartType())) {
            return "MULTI_VENDOR";
        }
        
        return cart.getCartType();
    }
    
    private BigDecimal calculateItemsWeight(List<CartItem> items) {
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product.getWeight() != null) {
                BigDecimal itemWeight = product.getWeight()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                totalWeight = totalWeight.add(itemWeight);
            }
        }
        return totalWeight.compareTo(BigDecimal.ZERO) > 0 ? totalWeight : BigDecimal.ONE;
    }
    
    private CartResponse mapToResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        
        List<com.vendo.cart.dto.response.CartItemResponse> itemResponses = items.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
        
        int totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        BigDecimal subtotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal shipping = BigDecimal.ZERO; // Will be calculated at checkout
        BigDecimal total = subtotal.add(tax).add(shipping);
        
        return CartResponse.builder()
                .userId(cart.getUser().getId())
                .cartType(cart.getCartType())
                .businessAccountId(cart.getBusinessAccountId())
                .items(itemResponses)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .tax(tax)
                .shipping(shipping)
                .total(total)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
    
    private com.vendo.cart.dto.response.CartItemResponse mapItemToResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        
        return com.vendo.cart.dto.response.CartItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .productImage(product.getImage() != null ? product.getImage() : null)
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .quantity(item.getQuantity())
                .itemTotal(itemTotal)
                .retailerId(item.getRetailer() != null ? item.getRetailer().getId() : null)
                .retailerName(item.getRetailer() != null ? item.getRetailer().getName() : null)
                .build();
    }
}
