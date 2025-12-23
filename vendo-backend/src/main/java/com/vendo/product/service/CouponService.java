package com.vendo.product.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.CouponRequest;
import com.vendo.product.dto.response.CouponResponse;
import com.vendo.product.entity.Category;
import com.vendo.product.entity.Coupon;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.CategoryRepository;
import com.vendo.product.repository.CouponRepository;
import com.vendo.product.repository.ProductRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing coupons and discounts.
 * Handles coupon validation, application, and discount calculation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {
    
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    
    /**
     * Validate a coupon code.
     * Checks if the coupon exists, is active, not expired, and within usage limits.
     * 
     * @param code Coupon code to validate
     * @param userId Optional user ID for user-specific validation
     * @param orderAmount Optional order amount for minimum purchase validation
     * @return CouponResponse with validation details
     */
    @Transactional(readOnly = true)
    public CouponResponse validateCoupon(String code, Long userId, BigDecimal orderAmount) {
        log.info("Validating coupon code: {}", code);
        
        LocalDateTime now = LocalDateTime.now();
        Coupon coupon = couponRepository.findValidCouponByCode(code, now)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));
        
        CouponResponse.CouponResponseBuilder responseBuilder = CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumPurchase(coupon.getMinimumPurchase())
                .maximumDiscount(coupon.getMaximumDiscount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .applicableTo(coupon.getApplicableTo())
                .isActive(coupon.getIsActive())
                .createdAt(coupon.getCreatedAt());
        
        // Validate expiration dates
        if (now.isBefore(coupon.getValidFrom())) {
            responseBuilder.isValid(false)
                    .validationMessage("Coupon is not yet valid. Valid from: " + coupon.getValidFrom());
            return responseBuilder.build();
        }
        
        if (now.isAfter(coupon.getValidUntil())) {
            responseBuilder.isValid(false)
                    .validationMessage("Coupon has expired. Valid until: " + coupon.getValidUntil());
            return responseBuilder.build();
        }
        
        // Validate usage limit
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            responseBuilder.isValid(false)
                    .validationMessage("Coupon usage limit has been reached");
            return responseBuilder.build();
        }
        
        // Validate minimum purchase
        if (orderAmount != null && coupon.getMinimumPurchase() != null) {
            if (orderAmount.compareTo(coupon.getMinimumPurchase()) < 0) {
                responseBuilder.isValid(false)
                        .validationMessage(String.format("Minimum purchase of %s required", coupon.getMinimumPurchase()));
                return responseBuilder.build();
            }
        }
        
        // Validate applicable to user type
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            String userType = user.getUserType();
            if (coupon.getApplicableTo() != null && !coupon.getApplicableTo().equals("ALL")) {
                if (coupon.getApplicableTo().equals("B2C") && !userType.equals("CUSTOMER")) {
                    responseBuilder.isValid(false)
                            .validationMessage("This coupon is only applicable to B2C customers");
                    return responseBuilder.build();
                }
                if (coupon.getApplicableTo().equals("B2B") && !userType.equals("BUSINESS")) {
                    responseBuilder.isValid(false)
                            .validationMessage("This coupon is only applicable to B2B customers");
                    return responseBuilder.build();
                }
            }
        }
        
        responseBuilder.isValid(true)
                .validationMessage("Coupon is valid");
        
        return responseBuilder.build();
    }
    
    /**
     * Apply a coupon to an order and calculate the discount.
     * 
     * @param code Coupon code
     * @param orderAmount Original order amount
     * @param userId User ID applying the coupon
     * @param productIds Optional list of product IDs in the order (for product-specific coupons)
     * @param categoryIds Optional list of category IDs in the order (for category-specific coupons)
     * @return CouponResponse with calculated discount details
     */
    @Transactional
    public CouponResponse applyCoupon(String code, BigDecimal orderAmount, Long userId, 
                                     List<Long> productIds, List<Long> categoryIds) {
        log.info("Applying coupon code: {} to order amount: {} for user: {}", code, orderAmount, userId);
        
        // First validate the coupon
        CouponResponse validation = validateCoupon(code, userId, orderAmount);
        if (!validation.getIsValid()) {
            throw new BadRequestException(validation.getValidationMessage());
        }
        
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));
        
        // Check if coupon is applicable to specific products or categories
        if (coupon.getApplicableTo() != null) {
            if (coupon.getApplicableTo().equals("SPECIFIC_PRODUCTS")) {
                if (productIds == null || productIds.isEmpty()) {
                    throw new BadRequestException("This coupon requires specific products");
                }
                List<Long> couponProductIds = coupon.getProducts().stream()
                        .map(Product::getId)
                        .collect(Collectors.toList());
                boolean hasApplicableProduct = productIds.stream()
                        .anyMatch(couponProductIds::contains);
                if (!hasApplicableProduct) {
                    throw new BadRequestException("This coupon is not applicable to the products in your order");
                }
            }
            
            if (coupon.getApplicableTo().equals("SPECIFIC_CATEGORIES")) {
                if (categoryIds == null || categoryIds.isEmpty()) {
                    throw new BadRequestException("This coupon requires specific categories");
                }
                List<Long> couponCategoryIds = coupon.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toList());
                boolean hasApplicableCategory = categoryIds.stream()
                        .anyMatch(couponCategoryIds::contains);
                if (!hasApplicableCategory) {
                    throw new BadRequestException("This coupon is not applicable to the categories in your order");
                }
            }
        }
        
        // Calculate discount based on coupon type
        BigDecimal calculatedDiscount = calculateDiscount(coupon, orderAmount);
        
        // Build response
        CouponResponse response = mapToResponse(coupon);
        response.setOriginalAmount(orderAmount);
        response.setCalculatedDiscount(calculatedDiscount);
        response.setFinalAmount(orderAmount.subtract(calculatedDiscount));
        response.setIsValid(true);
        response.setValidationMessage("Coupon applied successfully");
        
        // Increment usage count
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
        
        log.info("Coupon {} applied successfully. Discount: {}, Final amount: {}", 
                code, calculatedDiscount, response.getFinalAmount());
        
        return response;
    }
    
    /**
     * Calculate discount amount based on coupon type.
     * 
     * @param coupon Coupon entity
     * @param orderAmount Original order amount
     * @return Calculated discount amount
     */
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;
        
        switch (coupon.getDiscountType().toUpperCase()) {
            case "PERCENTAGE":
                discount = orderAmount.multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                
                // Apply maximum discount limit if set
                if (coupon.getMaximumDiscount() != null && discount.compareTo(coupon.getMaximumDiscount()) > 0) {
                    discount = coupon.getMaximumDiscount();
                }
                break;
                
            case "FIXED_AMOUNT":
                discount = coupon.getDiscountValue();
                // Ensure discount doesn't exceed order amount
                if (discount.compareTo(orderAmount) > 0) {
                    discount = orderAmount;
                }
                break;
                
            case "FREE_SHIPPING":
                // Free shipping discount is typically handled separately
                // For now, return zero as shipping is calculated separately
                discount = BigDecimal.ZERO;
                break;
                
            default:
                throw new BadRequestException("Invalid discount type: " + coupon.getDiscountType());
        }
        
        return discount;
    }
    
    /**
     * Get all available coupons for a user.
     * Returns active, non-expired coupons that are applicable to the user.
     * 
     * @param userId Optional user ID to filter by user type
     * @return List of available CouponResponse
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons(Long userId) {
        log.info("Fetching available coupons for user: {}", userId);
        
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> coupons = couponRepository.findAvailableCoupons(now);
        
        // Filter by user type if userId is provided
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            String userType = user.getUserType();
            coupons = coupons.stream()
                    .filter(coupon -> {
                        String applicableTo = coupon.getApplicableTo();
                        return applicableTo == null || 
                               applicableTo.equals("ALL") ||
                               (applicableTo.equals("B2C") && userType.equals("CUSTOMER")) ||
                               (applicableTo.equals("B2B") && userType.equals("BUSINESS"));
                    })
                    .collect(Collectors.toList());
        }
        
        return coupons.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new coupon.
     * 
     * @param request CouponRequest with coupon details
     * @return CouponResponse with created coupon details
     */
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        log.info("Creating new coupon with code: {}", request.getCode());
        
        // Check if coupon code already exists
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists: " + request.getCode());
        }
        
        // Validate date range
        if (request.getValidUntil().isBefore(request.getValidFrom()) || 
            request.getValidUntil().isEqual(request.getValidFrom())) {
            throw new BadRequestException("Valid until date must be after valid from date");
        }
        
        // Build coupon entity
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumPurchase(request.getMinimumPurchase())
                .maximumDiscount(request.getMaximumDiscount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .applicableTo(request.getApplicableTo() != null ? request.getApplicableTo() : "ALL")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        
        // Add products if specified
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getProductIds());
            if (products.size() != request.getProductIds().size()) {
                throw new BadRequestException("Some product IDs are invalid");
            }
            coupon.setProducts(products);
        }
        
        // Add categories if specified
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.size() != request.getCategoryIds().size()) {
                throw new BadRequestException("Some category IDs are invalid");
            }
            coupon.setCategories(categories);
        }
        
        coupon = couponRepository.save(coupon);
        log.info("Coupon created successfully with ID: {}", coupon.getId());
        
        return mapToResponse(coupon);
    }
    
    /**
     * Update an existing coupon.
     * 
     * @param id Coupon ID
     * @param request CouponRequest with updated coupon details
     * @return CouponResponse with updated coupon details
     */
    @Transactional
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        log.info("Updating coupon with ID: {}", id);
        
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        
        // Check if code is being changed and if new code already exists
        if (!coupon.getCode().equals(request.getCode()) && couponRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Coupon code already exists: " + request.getCode());
        }
        
        // Validate date range
        if (request.getValidUntil().isBefore(request.getValidFrom()) || 
            request.getValidUntil().isEqual(request.getValidFrom())) {
            throw new BadRequestException("Valid until date must be after valid from date");
        }
        
        // Update coupon fields
        coupon.setCode(request.getCode());
        coupon.setName(request.getName());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumPurchase(request.getMinimumPurchase());
        coupon.setMaximumDiscount(request.getMaximumDiscount());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setValidFrom(request.getValidFrom());
        coupon.setValidUntil(request.getValidUntil());
        coupon.setApplicableTo(request.getApplicableTo() != null ? request.getApplicableTo() : "ALL");
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }
        
        // Update products if specified
        if (request.getProductIds() != null) {
            if (request.getProductIds().isEmpty()) {
                coupon.setProducts(new ArrayList<>());
            } else {
                List<Product> products = productRepository.findAllById(request.getProductIds());
                if (products.size() != request.getProductIds().size()) {
                    throw new BadRequestException("Some product IDs are invalid");
                }
                coupon.setProducts(products);
            }
        }
        
        // Update categories if specified
        if (request.getCategoryIds() != null) {
            if (request.getCategoryIds().isEmpty()) {
                coupon.setCategories(new ArrayList<>());
            } else {
                List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
                if (categories.size() != request.getCategoryIds().size()) {
                    throw new BadRequestException("Some category IDs are invalid");
                }
                coupon.setCategories(categories);
            }
        }
        
        coupon = couponRepository.save(coupon);
        log.info("Coupon updated successfully with ID: {}", coupon.getId());
        
        return mapToResponse(coupon);
    }
    
    /**
     * Get a coupon by ID.
     * 
     * @param id Coupon ID
     * @return CouponResponse with coupon details
     */
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(Long id) {
        log.info("Fetching coupon with ID: {}", id);
        
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        
        return mapToResponse(coupon);
    }
    
    /**
     * Delete a coupon by ID.
     * 
     * @param id Coupon ID
     */
    @Transactional
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon with ID: {}", id);
        
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
        
        couponRepository.delete(coupon);
        log.info("Coupon deleted successfully with ID: {}", id);
    }
    
    /**
     * Map Coupon entity to CouponResponse.
     * 
     * @param coupon Coupon entity
     * @return CouponResponse
     */
    private CouponResponse mapToResponse(Coupon coupon) {
        List<Long> productIds = coupon.getProducts() != null ?
                coupon.getProducts().stream().map(Product::getId).collect(Collectors.toList()) :
                new ArrayList<>();
        
        List<Long> categoryIds = coupon.getCategories() != null ?
                coupon.getCategories().stream().map(Category::getId).collect(Collectors.toList()) :
                new ArrayList<>();
        
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumPurchase(coupon.getMinimumPurchase())
                .maximumDiscount(coupon.getMaximumDiscount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .applicableTo(coupon.getApplicableTo())
                .isActive(coupon.getIsActive())
                .createdAt(coupon.getCreatedAt())
                .productIds(productIds)
                .categoryIds(categoryIds)
                .build();
    }
}
