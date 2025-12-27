package com.vendo.b2b.service;

import com.vendo.b2b.dto.response.PricingResponse;
import com.vendo.b2b.entity.BusinessAccount;
import com.vendo.b2b.repository.BusinessAccountRepository;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.entity.B2BPricingTier;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.B2BPricingTierRepository;
import com.vendo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class B2BPricingService {
    
    private final ProductRepository productRepository;
    private final B2BPricingTierRepository b2BPricingTierRepository;
    private final BusinessAccountRepository businessAccountRepository;
    
    /**
     * Get B2B pricing for a product based on productId, businessAccountId, and quantity
     * This is the main method as specified in the requirements
     * Results are cached for 30 minutes using Redis
     */
    @Cacheable(value = "b2bPricing", key = "#productId + '_' + (#businessAccountId != null ? #businessAccountId : 'null') + '_' + #quantity")
    @Transactional(readOnly = true)
    public PricingResponse getB2BPricing(Long productId, Long businessAccountId, Integer quantity) {
        log.debug("Getting B2B pricing for product: {}, businessAccountId: {}, quantity: {}", 
                productId, businessAccountId, quantity);
        
        // Get business account if provided
        BusinessAccount businessAccount = null;
        if (businessAccountId != null) {
            businessAccount = businessAccountRepository.findById(businessAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("BusinessAccount", "id", businessAccountId));
        }
        
        return calculateB2BPricing(productId, businessAccount, quantity);
    }
    
    /**
     * Get B2B pricing for a product based on quantity and business type
     * This method accepts BusinessAccount object directly (for internal use)
     * Results are cached for 30 minutes using Redis
     */
    @Cacheable(value = "b2bPricing", key = "#productId + '_' + (#businessAccount != null ? #businessAccount.getId() : 'null') + '_' + #quantity + '_' + (#businessAccount != null ? #businessAccount.getBusinessType() : 'null')")
    @Transactional(readOnly = true)
    public PricingResponse getB2BPricing(Long productId, BusinessAccount businessAccount, Integer quantity) {
        return calculateB2BPricing(productId, businessAccount, quantity);
    }
    
    /**
     * Internal method to calculate B2B pricing
     * This method performs the actual calculation logic
     */
    private PricingResponse calculateB2BPricing(Long productId, BusinessAccount businessAccount, Integer quantity) {
        log.debug("Calculating B2B pricing for product: {}, quantity: {}", productId, quantity);
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        
        // Get business type from account or use default
        String businessType = businessAccount != null ? businessAccount.getBusinessType() : null;
        
        // Get original price from product
        BigDecimal originalPrice = product.getPrice();
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Product price is not set or invalid");
        }
        
        // Find applicable pricing tier
        Optional<B2BPricingTier> tierOptional = Optional.empty();
        
        if (businessType != null) {
            // Try to find tier by business type and quantity
            tierOptional = b2BPricingTierRepository.findApplicableTier(productId, businessType, quantity);
        }
        
        // If no tier found by business type, try to find any applicable tier
        if (tierOptional.isEmpty()) {
            List<B2BPricingTier> applicableTiers = b2BPricingTierRepository
                    .findApplicableTiersByProductAndQuantity(productId, quantity);
            
            if (!applicableTiers.isEmpty()) {
                // Get the tier with highest minQuantity (best discount)
                tierOptional = applicableTiers.stream()
                        .max(Comparator.comparing(B2BPricingTier::getMinQuantity));
            }
        }
        
        BigDecimal unitPrice = originalPrice;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountPercentage = BigDecimal.ZERO;
        String pricingTierName = null;
        
        if (tierOptional.isPresent()) {
            B2BPricingTier tier = tierOptional.get();
            
            // Use tier price if available, otherwise calculate from discount
            if (tier.getPrice() != null && tier.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                unitPrice = tier.getPrice();
                discountAmount = originalPrice.subtract(unitPrice);
            } else if (tier.getDiscountPercentage() != null && tier.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
                discountPercentage = tier.getDiscountPercentage();
                discountAmount = originalPrice.multiply(discountPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                unitPrice = originalPrice.subtract(discountAmount);
            }
            
            pricingTierName = String.format("Tier %d-%d", tier.getMinQuantity(), 
                    tier.getMaxQuantity() != null ? tier.getMaxQuantity() : Integer.MAX_VALUE);
        }
        
        // Calculate total price
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Calculate discount percentage if not already set
        if (discountPercentage.compareTo(BigDecimal.ZERO) == 0 && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            discountPercentage = discountAmount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        log.debug("B2B pricing calculated - Unit: {}, Total: {}, Discount: {}%", 
                unitPrice, totalPrice, discountPercentage);
        
        return PricingResponse.builder()
                .productId(productId)
                .businessAccountId(businessAccount != null ? businessAccount.getId() : null)
                .businessType(businessType)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .originalPrice(originalPrice)
                .discountAmount(discountAmount.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP))
                .discountPercentage(discountPercentage)
                .totalPrice(totalPrice)
                .pricingTierName(pricingTierName)
                .build();
    }
    
    /**
     * Evict all cached pricing for a specific product
     * Call this when pricing tiers for a product are updated
     */
    @CacheEvict(value = "b2bPricing", allEntries = true)
    public void evictPricingCacheForProduct(Long productId) {
        log.debug("Evicting B2B pricing cache for product: {}", productId);
    }
    
    /**
     * Evict all cached pricing for a specific business account
     * Call this when business account type or status changes
     */
    @CacheEvict(value = "b2bPricing", allEntries = true)
    public void evictPricingCacheForBusinessAccount(Long businessAccountId) {
        log.debug("Evicting B2B pricing cache for business account: {}", businessAccountId);
    }
    
    /**
     * Evict all B2B pricing cache
     * Call this when pricing tiers are updated globally
     */
    @CacheEvict(value = "b2bPricing", allEntries = true)
    public void evictAllPricingCache() {
        log.debug("Evicting all B2B pricing cache");
    }
}

