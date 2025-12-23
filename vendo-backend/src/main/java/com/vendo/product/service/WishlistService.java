package com.vendo.product.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.response.ProductResponse;
import com.vendo.product.dto.response.WishlistResponse;
import com.vendo.product.entity.Category;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductCollection;
import com.vendo.product.entity.ProductTag;
import com.vendo.product.entity.Wishlist;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductReviewRepository;
import com.vendo.product.repository.WishlistRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing wishlist items per user.
 * Tracks products that users want to save for later.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final UserRepository userRepository;
    
    /**
     * Add a product to the user's wishlist.
     * If the product is already in the wishlist, returns the existing wishlist item.
     * 
     * @param customerId Customer ID
     * @param productId Product ID to add
     * @return WishlistResponse with product details
     */
    @Transactional
    public WishlistResponse addToWishlist(Long customerId, Long productId) {
        log.info("Adding product {} to wishlist for customer {}", productId, customerId);
        
        // Validate user exists
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        // Check if product is already in wishlist
        if (wishlistRepository.existsByCustomerIdAndProductId(customerId, productId)) {
            log.debug("Product {} already in wishlist for customer {}", productId, customerId);
            // Return existing wishlist item
            Wishlist existing = wishlistRepository.findByCustomerIdWithProduct(customerId).stream()
                    .filter(w -> w.getProductId().equals(productId))
                    .findFirst()
                    .orElse(null);
            
            if (existing != null) {
                return mapToResponse(existing, product);
            }
        }
        
        // Create new wishlist entry
        Wishlist wishlist = Wishlist.builder()
                .customerId(customerId)
                .productId(productId)
                .build();
        
        wishlist = wishlistRepository.save(wishlist);
        log.info("Product {} added to wishlist for customer {}", productId, customerId);
        
        return mapToResponse(wishlist, product);
    }
    
    /**
     * Remove a product from the user's wishlist.
     * 
     * @param customerId Customer ID
     * @param productId Product ID to remove
     */
    @Transactional
    public void removeFromWishlist(Long customerId, Long productId) {
        log.info("Removing product {} from wishlist for customer {}", productId, customerId);
        
        // Check if wishlist item exists
        if (!wishlistRepository.existsByCustomerIdAndProductId(customerId, productId)) {
            throw new ResourceNotFoundException("Wishlist item", "productId", productId);
        }
        
        wishlistRepository.deleteByCustomerIdAndProductId(customerId, productId);
        log.info("Product {} removed from wishlist for customer {}", productId, customerId);
    }
    
    /**
     * Get all wishlist items for a user.
     * Returns list of WishlistResponse with product details.
     * 
     * @param customerId Customer ID
     * @return List of WishlistResponse with product details
     */
    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(Long customerId) {
        log.info("Fetching wishlist for customer {}", customerId);
        
        // Fetch wishlist items with products (using JOIN FETCH to avoid N+1 queries)
        List<Wishlist> wishlistItems = wishlistRepository.findByCustomerIdWithProduct(customerId);
        
        // Map to response with product details
        return wishlistItems.stream()
                .map(wishlist -> {
                    Product product = wishlist.getProduct();
                    if (product == null) {
                        // If product was deleted, fetch it separately or skip
                        product = productRepository.findById(wishlist.getProductId()).orElse(null);
                    }
                    return mapToResponse(wishlist, product);
                })
                .filter(response -> response.getProduct() != null) // Filter out deleted products
                .collect(Collectors.toList());
    }
    
    /**
     * Clear all wishlist items for a user.
     * 
     * @param customerId Customer ID
     */
    @Transactional
    public void clearWishlist(Long customerId) {
        log.info("Clearing wishlist for customer {}", customerId);
        
        wishlistRepository.deleteByCustomerId(customerId);
        log.info("Wishlist cleared for customer {}", customerId);
    }
    
    /**
     * Check if a product is in the user's wishlist.
     * 
     * @param customerId Customer ID
     * @param productId Product ID to check
     * @return true if product is in wishlist, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long customerId, Long productId) {
        return wishlistRepository.existsByCustomerIdAndProductId(customerId, productId);
    }
    
    /**
     * Map Wishlist entity to WishlistResponse.
     * Includes product details and timestamps.
     * 
     * @param wishlist Wishlist entity
     * @param product Product entity (can be null if product was deleted)
     * @return WishlistResponse
     */
    private WishlistResponse mapToResponse(Wishlist wishlist, Product product) {
        WishlistResponse.WishlistResponseBuilder builder = WishlistResponse.builder()
                .customerId(wishlist.getCustomerId())
                .productId(wishlist.getProductId())
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt());
        
        // Map product details if product exists
        if (product != null) {
            ProductResponse productResponse = mapProductToResponse(product);
            builder.product(productResponse);
        }
        
        return builder.build();
    }
    
    /**
     * Map Product entity to ProductResponse with rating information.
     * 
     * @param product Product entity
     * @return ProductResponse
     */
    private ProductResponse mapProductToResponse(Product product) {
        // Get rating information
        Double averageRating = productReviewRepository.calculateAverageRating(product.getId());
        Long reviewCount = productReviewRepository.countByProductId(product.getId());
        
        // Map product to response
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .content(product.getContent())
                .status(product.getStatus())
                .images(product.getImages())
                .videoMedia(product.getVideoMedia())
                .sku(product.getSku())
                .isFeatured(product.getIsFeatured())
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .isVariation(product.getIsVariation())
                .saleType(product.getSaleType())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .startDate(product.getStartDate())
                .endDate(product.getEndDate())
                .length(product.getLength())
                .width(product.getWide()) // Product entity uses "wide" field
                .height(product.getHeight())
                .weight(product.getWeight())
                .taxId(product.getTaxId())
                .categoryIds(product.getCategories() != null ? 
                        product.getCategories().stream().map(Category::getId).collect(Collectors.toList()) : null)
                .categoryNames(product.getCategories() != null ? 
                        product.getCategories().stream().map(Category::getName).collect(Collectors.toList()) : null)
                .tagIds(product.getTags() != null ? 
                        product.getTags().stream().map(ProductTag::getId).collect(Collectors.toList()) : null)
                .tagNames(product.getTags() != null ? 
                        product.getTags().stream().map(ProductTag::getName).collect(Collectors.toList()) : null)
                .collectionIds(product.getCollections() != null ? 
                        product.getCollections().stream().map(c -> c.getId()).collect(Collectors.toList()) : null)
                .collectionNames(product.getCollections() != null ? 
                        product.getCollections().stream().map(c -> c.getName()).collect(Collectors.toList()) : null)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
