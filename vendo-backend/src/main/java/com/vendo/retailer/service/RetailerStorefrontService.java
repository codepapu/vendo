package com.vendo.retailer.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.product.entity.ProductRetailer;
import com.vendo.product.entity.RetailerReview;
import com.vendo.product.repository.ProductRetailerRepository;
import com.vendo.product.repository.RetailerReviewRepository;
import com.vendo.retailer.dto.request.StorefrontUpdateRequest;
import com.vendo.retailer.dto.response.StorefrontResponse;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailerStorefrontService {
    
    private final RetailerRepository retailerRepository;
    private final ProductRetailerRepository productRetailerRepository;
    private final RetailerReviewRepository retailerReviewRepository;
    private final RetailerService retailerService;
    
    /**
     * Get public storefront for a retailer by slug
     */
    @Transactional(readOnly = true)
    public StorefrontResponse getStorefront(String slug) {
        Retailer retailer = retailerRepository.findByStoreSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "storeSlug", slug));
        
        // Only show ACTIVE retailers
        if (!"ACTIVE".equals(retailer.getStatus())) {
            throw new ResourceNotFoundException("Retailer", "storeSlug", slug);
        }
        
        return buildStorefrontResponse(retailer);
    }
    
    /**
     * Get public storefront for a retailer by ID
     */
    @Transactional(readOnly = true)
    public StorefrontResponse getStorefrontById(Long retailerId) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        // Only show ACTIVE retailers
        if (!"ACTIVE".equals(retailer.getStatus())) {
            throw new ResourceNotFoundException("Retailer", "id", retailerId);
        }
        
        return buildStorefrontResponse(retailer);
    }
    
    /**
     * Update storefront information (for retailer owners)
     */
    @Transactional
    public StorefrontResponse updateStorefront(Long retailerId, StorefrontUpdateRequest request) {
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        
        if (request.getDescription() != null) {
            retailer.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            retailer.setLogoUrl(request.getLogoUrl());
        }
        if (request.getBannerUrl() != null) {
            retailer.setBannerUrl(request.getBannerUrl());
        }
        
        Retailer updatedRetailer = retailerRepository.save(retailer);
        log.info("Storefront updated for retailer: {}", retailerId);
        
        return buildStorefrontResponse(updatedRetailer);
    }
    
    /**
     * Get products for a retailer's storefront
     */
    @Transactional(readOnly = true)
    public Page<ProductRetailerResponse> getStorefrontProducts(String slug, Pageable pageable) {
        Retailer retailer = retailerRepository.findByStoreSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "storeSlug", slug));
        
        // Only show ACTIVE retailers
        if (!"ACTIVE".equals(retailer.getStatus())) {
            throw new ResourceNotFoundException("Retailer", "storeSlug", slug);
        }
        
        // Get only active products
        Page<ProductRetailer> products = productRetailerRepository.findByRetailerIdAndIsActive(
                retailer.getId(), true, pageable);
        
        return products.map(this::mapToProductRetailerResponse);
    }
    
    /**
     * Search retailers by name or description
     */
    @Transactional(readOnly = true)
    public Page<StorefrontResponse> searchRetailers(String searchTerm, Pageable pageable) {
        // Search by store name or description containing search term
        Page<Retailer> retailers = retailerRepository.findByStoreNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm, pageable);
        
        // Filter to only ACTIVE retailers
        List<Retailer> activeRetailers = retailers.getContent().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .collect(Collectors.toList());
        
        // Convert to StorefrontResponse
        List<StorefrontResponse> storefronts = activeRetailers.stream()
                .map(this::buildStorefrontResponse)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
                storefronts,
                pageable,
                activeRetailers.size()
        );
    }
    
    /**
     * Build StorefrontResponse from Retailer entity
     */
    private StorefrontResponse buildStorefrontResponse(Retailer retailer) {
        // Get active products count
        List<ProductRetailer> activeProducts = productRetailerRepository.findByRetailerIdAndIsActiveTrue(retailer.getId());
        
        // Get reviews
        List<RetailerReview> reviews = retailerReviewRepository.findByRetailerIdAndIsApprovedTrue(retailer.getId());
        
        // Calculate average rating
        BigDecimal averageRating = BigDecimal.ZERO;
        if (!reviews.isEmpty()) {
            double avg = reviews.stream()
                    .mapToInt(RetailerReview::getRating)
                    .average()
                    .orElse(0.0);
            averageRating = BigDecimal.valueOf(avg).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        
        return StorefrontResponse.builder()
                .retailerId(retailer.getId())
                .storeName(retailer.getStoreName())
                .storeSlug(retailer.getStoreSlug())
                .description(retailer.getDescription())
                .logoUrl(retailer.getLogoUrl())
                .bannerUrl(retailer.getBannerUrl())
                .rating(averageRating)
                .totalSales(retailer.getTotalSales())
                .totalOrders(retailer.getTotalOrders())
                .productCount(activeProducts.size())
                .reviewCount(reviews.size())
                .verified(retailer.getVerified())
                .build();
    }
    
    /**
     * Validate retailer ownership
     */
    public void validateRetailerOwnership(Long retailerId, Long userId) {
        retailerService.validateRetailerOwnership(retailerId, userId);
    }
    
    /**
     * Map ProductRetailer to ProductRetailerResponse
     */
    private ProductRetailerResponse mapToProductRetailerResponse(ProductRetailer productRetailer) {
        ProductRetailerResponse.ProductInfo productInfo = null;
        if (productRetailer.getProduct() != null) {
            productInfo = ProductRetailerResponse.ProductInfo.builder()
                    .id(productRetailer.getProduct().getId())
                    .name(productRetailer.getProduct().getName())
                    .sku(productRetailer.getProduct().getSku())
                    .build();
        }
        
        ProductRetailerResponse.RetailerInfo retailerInfo = null;
        if (productRetailer.getRetailer() != null) {
            retailerInfo = ProductRetailerResponse.RetailerInfo.builder()
                    .id(productRetailer.getRetailer().getId())
                    .storeName(productRetailer.getRetailer().getStoreName())
                    .storeSlug(productRetailer.getRetailer().getStoreSlug())
                    .build();
        }
        
        boolean isLowStock = productRetailer.getStockQuantity() <= productRetailer.getLowStockThreshold();
        
        return ProductRetailerResponse.builder()
                .id(productRetailer.getId())
                .productId(productRetailer.getProduct() != null ? productRetailer.getProduct().getId() : null)
                .product(productInfo)
                .retailerId(productRetailer.getRetailer() != null ? productRetailer.getRetailer().getId() : null)
                .retailer(retailerInfo)
                .price(productRetailer.getPrice())
                .compareAtPrice(productRetailer.getCompareAtPrice())
                .costPrice(productRetailer.getCostPrice())
                .stockQuantity(productRetailer.getStockQuantity())
                .lowStockThreshold(productRetailer.getLowStockThreshold())
                .isActive(productRetailer.getIsActive())
                .isLowStock(isLowStock)
                .createdAt(productRetailer.getCreatedAt())
                .updatedAt(productRetailer.getUpdatedAt())
                .build();
    }
}

