package com.vendo.user.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.response.ProductResponse;
import com.vendo.product.entity.Category;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductCollection;
import com.vendo.product.entity.ProductTag;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductReviewRepository;
import com.vendo.user.dto.response.RecentlyViewedResponse;
import com.vendo.user.entity.CustomerRecentlyViewedProduct;
import com.vendo.user.repository.CustomerRecentlyViewedProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing recently viewed products per user.
 * Tracks product views with timestamps and limits to the last 20 viewed products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecentlyViewedService {
    
    private final CustomerRecentlyViewedProductRepository recentlyViewedRepository;
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    
    private static final int MAX_RECENTLY_VIEWED = 20;
    
    /**
     * Add a product to the user's recently viewed list.
     * If the product is already in the list, updates the timestamp.
     * Maintains a limit of MAX_RECENTLY_VIEWED products per user.
     * 
     * @param customerId Customer ID
     * @param productId Product ID to add
     * @return RecentlyViewedResponse with product details and view date
     */
    @Transactional
    public RecentlyViewedResponse addToRecentlyViewed(Long customerId, Long productId) {
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        // Check if product is already in recently viewed
        CustomerRecentlyViewedProduct existing = recentlyViewedRepository
                .findByCustomerIdAndProductId(customerId, productId)
                .orElse(null);
        
        CustomerRecentlyViewedProduct recentlyViewed;
        if (existing != null) {
            // Delete existing entry and create a new one to update the timestamp
            // This ensures the product appears at the top of the recently viewed list
            recentlyViewedRepository.delete(existing);
            log.debug("Removed existing recently viewed entry for customer ID: {}, product ID: {}", customerId, productId);
        }
        
        // Create new entry (or recreate if it existed)
        recentlyViewed = CustomerRecentlyViewedProduct.builder()
                .customerId(customerId)
                .productId(productId)
                .build();
        recentlyViewed = recentlyViewedRepository.save(recentlyViewed);
        log.debug("Added recently viewed entry for customer ID: {}, product ID: {}", customerId, productId);
        
        // Maintain limit of MAX_RECENTLY_VIEWED products
        maintainRecentlyViewedLimit(customerId);
        
        // Map to response
        return mapToResponse(recentlyViewed, product);
    }
    
    /**
     * Get the user's recently viewed products.
     * Returns up to MAX_RECENTLY_VIEWED products, ordered by most recent first.
     * 
     * @param customerId Customer ID
     * @return List of RecentlyViewedResponse with product details and view dates
     */
    @Transactional(readOnly = true)
    public List<RecentlyViewedResponse> getRecentlyViewed(Long customerId) {
        // Get recently viewed products, limited to MAX_RECENTLY_VIEWED
        Pageable pageable = PageRequest.of(0, MAX_RECENTLY_VIEWED, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<CustomerRecentlyViewedProduct> recentlyViewedList = recentlyViewedRepository
                .findRecentByCustomerId(customerId, pageable);
        
        // Map to response with product details
        return recentlyViewedList.stream()
                .map(rv -> {
                    Product product = productRepository.findById(rv.getProductId())
                            .orElse(null);
                    return mapToResponse(rv, product);
                })
                .filter(response -> response.getProduct() != null) // Filter out deleted products
                .collect(Collectors.toList());
    }
    
    /**
     * Clear all recently viewed products for a user.
     * 
     * @param customerId Customer ID
     */
    @Transactional
    public void clearRecentlyViewed(Long customerId) {
        recentlyViewedRepository.deleteByCustomerId(customerId);
        log.info("Cleared recently viewed products for customer ID: {}", customerId);
    }
    
    /**
     * Maintain the limit of MAX_RECENTLY_VIEWED products per user.
     * Removes the oldest entries if the limit is exceeded.
     * 
     * @param customerId Customer ID
     */
    private void maintainRecentlyViewedLimit(Long customerId) {
        List<CustomerRecentlyViewedProduct> allRecent = recentlyViewedRepository
                .findRecentByCustomerId(customerId);
        
        if (allRecent.size() > MAX_RECENTLY_VIEWED) {
            // Get entries beyond the limit (oldest ones)
            List<CustomerRecentlyViewedProduct> toRemove = allRecent.subList(MAX_RECENTLY_VIEWED, allRecent.size());
            recentlyViewedRepository.deleteAll(toRemove);
            log.debug("Removed {} old recently viewed entries for customer ID: {}", toRemove.size(), customerId);
        }
    }
    
    /**
     * Map CustomerRecentlyViewedProduct entity to RecentlyViewedResponse.
     * Includes product details and view timestamp.
     * 
     * @param recentlyViewed CustomerRecentlyViewedProduct entity
     * @param product Product entity (can be null if product was deleted)
     * @return RecentlyViewedResponse
     */
    private RecentlyViewedResponse mapToResponse(CustomerRecentlyViewedProduct recentlyViewed, Product product) {
        RecentlyViewedResponse.RecentlyViewedResponseBuilder builder = RecentlyViewedResponse.builder()
                .id(recentlyViewed.getId())
                .customerId(recentlyViewed.getCustomerId())
                .productId(recentlyViewed.getProductId())
                .viewedAt(recentlyViewed.getCreatedAt())
                .createdAt(recentlyViewed.getCreatedAt());
        
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
                .width(product.getWide())
                .height(product.getHeight())
                .weight(product.getWeight())
                .taxId(product.getTaxId())
                .categoryIds(product.getCategories() != null ? 
                        product.getCategories().stream().map(c -> c.getId()).collect(Collectors.toList()) : null)
                .categoryNames(product.getCategories() != null ? 
                        product.getCategories().stream().map(c -> c.getName()).collect(Collectors.toList()) : null)
                .tagIds(product.getTags() != null ? 
                        product.getTags().stream().map(t -> t.getId()).collect(Collectors.toList()) : null)
                .tagNames(product.getTags() != null ? 
                        product.getTags().stream().map(t -> t.getName()).collect(Collectors.toList()) : null)
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
