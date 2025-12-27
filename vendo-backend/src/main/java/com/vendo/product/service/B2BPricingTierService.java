package com.vendo.product.service;

import com.vendo.b2b.service.B2BPricingService;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.entity.B2BPricingTier;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.B2BPricingTierRepository;
import com.vendo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for managing B2B Pricing Tiers
 * Handles CRUD operations and automatically evicts cache when tiers are updated
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class B2BPricingTierService {
    
    private final B2BPricingTierRepository b2BPricingTierRepository;
    private final ProductRepository productRepository;
    private final B2BPricingService b2BPricingService;
    
    /**
     * Create a new B2B pricing tier
     * Automatically evicts pricing cache for the product
     */
    @Transactional
    public B2BPricingTier createPricingTier(Long productId, String businessType, 
                                             Integer minQuantity, Integer maxQuantity,
                                             BigDecimal price, BigDecimal discountPercentage) {
        log.info("Creating B2B pricing tier for product: {}, businessType: {}, minQuantity: {}", 
                productId, businessType, minQuantity);
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        // Validate quantity ranges
        if (minQuantity == null || minQuantity <= 0) {
            throw new BadRequestException("Min quantity must be greater than 0");
        }
        
        if (maxQuantity != null && maxQuantity < minQuantity) {
            throw new BadRequestException("Max quantity must be greater than or equal to min quantity");
        }
        
        // Validate price or discount
        if (price == null && discountPercentage == null) {
            throw new BadRequestException("Either price or discount percentage must be provided");
        }
        
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Price must be non-negative");
        }
        
        if (discountPercentage != null && 
            (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || 
             discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BadRequestException("Discount percentage must be between 0 and 100");
        }
        
        // Check for overlapping tiers
        validateNoOverlappingTiers(productId, businessType, minQuantity, maxQuantity, null);
        
        // Create pricing tier
        B2BPricingTier pricingTier = B2BPricingTier.builder()
                .product(product)
                .businessType(businessType)
                .minQuantity(minQuantity)
                .maxQuantity(maxQuantity)
                .price(price)
                .discountPercentage(discountPercentage)
                .isActive(true)
                .build();
        
        B2BPricingTier savedTier = b2BPricingTierRepository.save(pricingTier);
        
        // Evict cache for this product
        b2BPricingService.evictPricingCacheForProduct(productId);
        
        log.info("B2B pricing tier created with ID: {}", savedTier.getId());
        return savedTier;
    }
    
    /**
     * Update an existing B2B pricing tier
     * Automatically evicts pricing cache for the product
     */
    @Transactional
    public B2BPricingTier updatePricingTier(Long tierId, String businessType,
                                           Integer minQuantity, Integer maxQuantity,
                                           BigDecimal price, BigDecimal discountPercentage,
                                           Boolean isActive) {
        log.info("Updating B2B pricing tier: {}", tierId);
        
        B2BPricingTier pricingTier = b2BPricingTierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("B2BPricingTier", "id", tierId));
        
        Long productId = pricingTier.getProduct().getId();
        
        // Validate quantity ranges if provided
        if (minQuantity != null) {
            if (minQuantity <= 0) {
                throw new BadRequestException("Min quantity must be greater than 0");
            }
            pricingTier.setMinQuantity(minQuantity);
        }
        
        if (maxQuantity != null) {
            if (maxQuantity < pricingTier.getMinQuantity()) {
                throw new BadRequestException("Max quantity must be greater than or equal to min quantity");
            }
            pricingTier.setMaxQuantity(maxQuantity);
        }
        
        // Validate price or discount
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Price must be non-negative");
        }
        
        if (discountPercentage != null && 
            (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || 
             discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BadRequestException("Discount percentage must be between 0 and 100");
        }
        
        // Check for overlapping tiers (excluding current tier)
        if (minQuantity != null || maxQuantity != null || businessType != null) {
            validateNoOverlappingTiers(productId, 
                    businessType != null ? businessType : pricingTier.getBusinessType(),
                    minQuantity != null ? minQuantity : pricingTier.getMinQuantity(),
                    maxQuantity != null ? maxQuantity : pricingTier.getMaxQuantity(),
                    tierId);
        }
        
        // Update fields
        if (businessType != null) {
            pricingTier.setBusinessType(businessType);
        }
        if (price != null) {
            pricingTier.setPrice(price);
        }
        if (discountPercentage != null) {
            pricingTier.setDiscountPercentage(discountPercentage);
        }
        if (isActive != null) {
            pricingTier.setIsActive(isActive);
        }
        
        B2BPricingTier updatedTier = b2BPricingTierRepository.save(pricingTier);
        
        // Evict cache for this product
        b2BPricingService.evictPricingCacheForProduct(productId);
        
        log.info("B2B pricing tier updated: {}", tierId);
        return updatedTier;
    }
    
    /**
     * Delete a B2B pricing tier
     * Automatically evicts pricing cache for the product
     */
    @Transactional
    public void deletePricingTier(Long tierId) {
        log.info("Deleting B2B pricing tier: {}", tierId);
        
        B2BPricingTier pricingTier = b2BPricingTierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("B2BPricingTier", "id", tierId));
        
        Long productId = pricingTier.getProduct().getId();
        
        b2BPricingTierRepository.delete(pricingTier);
        
        // Evict cache for this product
        b2BPricingService.evictPricingCacheForProduct(productId);
        
        log.info("B2B pricing tier deleted: {}", tierId);
    }
    
    /**
     * Get all pricing tiers for a product
     */
    @Transactional(readOnly = true)
    public List<B2BPricingTier> getPricingTiersByProduct(Long productId) {
        return b2BPricingTierRepository.findByProductId(productId);
    }
    
    /**
     * Get all pricing tiers for a business type
     */
    @Transactional(readOnly = true)
    public List<B2BPricingTier> getPricingTiersByBusinessType(String businessType) {
        return b2BPricingTierRepository.findByBusinessType(businessType);
    }
    
    /**
     * Get pricing tier by ID
     */
    @Transactional(readOnly = true)
    public B2BPricingTier getPricingTier(Long tierId) {
        return b2BPricingTierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("B2BPricingTier", "id", tierId));
    }
    
    /**
     * Validate that there are no overlapping quantity ranges for the same product and business type
     */
    private void validateNoOverlappingTiers(Long productId, String businessType,
                                            Integer minQuantity, Integer maxQuantity,
                                            Long excludeTierId) {
        List<B2BPricingTier> existingTiers = b2BPricingTierRepository
                .findByProductIdAndBusinessType(productId, businessType);
        
        for (B2BPricingTier existingTier : existingTiers) {
            // Skip the tier being updated
            if (excludeTierId != null && existingTier.getId().equals(excludeTierId)) {
                continue;
            }
            
            // Skip inactive tiers
            if (!existingTier.getIsActive()) {
                continue;
            }
            
            // Check for overlap
            Integer existingMin = existingTier.getMinQuantity();
            Integer existingMax = existingTier.getMaxQuantity();
            
            boolean overlaps = false;
            
            if (maxQuantity == null) {
                // New tier has no max, check if it overlaps with any existing tier
                if (existingMax == null) {
                    // Both have no max - they overlap if min quantities are the same
                    overlaps = existingMin.equals(minQuantity);
                } else {
                    // Existing has max, new doesn't - overlap if new min <= existing max
                    overlaps = minQuantity <= existingMax;
                }
            } else {
                // New tier has max
                if (existingMax == null) {
                    // Existing has no max - overlap if existing min <= new max
                    overlaps = existingMin <= maxQuantity;
                } else {
                    // Both have max - overlap if ranges intersect
                    overlaps = (minQuantity <= existingMax && maxQuantity >= existingMin);
                }
            }
            
            if (overlaps) {
                throw new BadRequestException(
                        String.format("Pricing tier overlaps with existing tier (ID: %d) for quantity range %d-%s",
                                existingTier.getId(),
                                existingMin,
                                existingMax != null ? existingMax.toString() : "unlimited"));
            }
        }
    }
}

