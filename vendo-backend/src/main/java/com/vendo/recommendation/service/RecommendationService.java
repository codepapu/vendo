package com.vendo.recommendation.service;

import com.vendo.product.dto.response.ProductResponse;
import com.vendo.product.entity.Category;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductTag;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductReviewRepository;
import com.vendo.product.service.ProductCatalogService;
import com.vendo.user.entity.CustomerRecentlyViewedProduct;
import com.vendo.user.repository.CustomerRecentlyViewedProductRepository;
import com.vendo.user.service.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Product Recommendation Service
 * Implements collaborative filtering, content-based filtering, and trending products
 * 
 * Integration Notes:
 * - Collaborative filtering: Find similar users based on purchase history
 * - Content-based filtering: Recommend products based on product attributes
 * - Recently viewed: Uses RecentlyViewedService
 * - Trending/Popular: Based on sales and reviews
 * 
 * Future Enhancements:
 * - ML service integration (optional, can be phase 2)
 * - Real-time recommendation updates
 * - A/B testing for recommendation algorithms
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductCatalogService productCatalogService;
    private final RecentlyViewedService recentlyViewedService;
    private final CustomerRecentlyViewedProductRepository recentlyViewedRepository;
    
    // TODO: Inject OrderRepository for purchase history-based recommendations
    // private final OrderRepository orderRepository;
    
    /**
     * Get recommended products for user (collaborative filtering + content-based)
     */
    @Cacheable(value = "recommendations", key = "'user_' + #userId + '_' + (#limit != null ? #limit : 10)")
    @Transactional(readOnly = true)
    public List<ProductResponse> getRecommendedProducts(Long userId, Integer limit) {
        log.info("Getting recommendations for user: {}", userId);
        
        if (userId == null) {
            log.warn("User ID is required for recommendations");
            return new ArrayList<>();
        }
        
        int recommendationLimit = limit != null && limit > 0 ? limit : 10;
        
        try {
            Set<Long> recommendedProductIds = new HashSet<>();
            
            // 1. Content-based recommendations (based on recently viewed products)
            List<ProductResponse> contentBased = getContentBasedRecommendations(userId, recommendationLimit);
            contentBased.forEach(p -> recommendedProductIds.add(p.getId()));
            
            // 2. Collaborative filtering recommendations (based on similar users)
            // TODO: Implement full collaborative filtering
            // For now, use a simplified approach based on popular products
            List<ProductResponse> collaborative = getCollaborativeRecommendations(userId, recommendationLimit);
            collaborative.forEach(p -> recommendedProductIds.add(p.getId()));
            
            // 3. Popular/trending products as fallback
            if (recommendedProductIds.size() < recommendationLimit) {
                List<ProductResponse> popular = getPopularProducts(recommendationLimit);
                popular.forEach(p -> {
                    if (!recommendedProductIds.contains(p.getId())) {
                        recommendedProductIds.add(p.getId());
                    }
                });
            }
            
            // Get final recommendations
            List<ProductResponse> allRecommendations = new ArrayList<>();
            allRecommendations.addAll(contentBased);
            
            // Add collaborative recommendations that aren't already included
            collaborative.forEach(p -> {
                if (!recommendedProductIds.contains(p.getId()) || 
                    recommendedProductIds.stream().noneMatch(id -> id.equals(p.getId()))) {
                    allRecommendations.add(p);
                }
            });
            
            // Limit to requested number
            return allRecommendations.stream()
                    .limit(recommendationLimit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting recommendations for user: {}", userId, e);
            // Return popular products as fallback
            return getPopularProducts(recommendationLimit);
        }
    }
    
    /**
     * Get similar products (content-based filtering)
     */
    @Cacheable(value = "recommendations", key = "'similar_' + #productId + '_' + (#limit != null ? #limit : 10)")
    @Transactional(readOnly = true)
    public List<ProductResponse> getSimilarProducts(Long productId, Integer limit) {
        log.info("Getting similar products for: {}", productId);
        
        if (productId == null) {
            log.warn("Product ID is required for similar products");
            return new ArrayList<>();
        }
        
        int recommendationLimit = limit != null && limit > 0 ? limit : 10;
        
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                log.warn("Product not found: {}", productId);
                return new ArrayList<>();
            }
            
            Set<Long> similarProductIds = new HashSet<>();
            similarProductIds.add(productId); // Exclude the product itself
            
            List<Product> similarProducts = new ArrayList<>();
            
            // Find products in same category
            if (product.getCategories() != null && !product.getCategories().isEmpty()) {
                Category primaryCategory = product.getCategories().get(0);
                List<Product> categoryProducts = productRepository.findByCategoryIdAndStatus(
                    primaryCategory.getId(), "published", PageRequest.of(0, recommendationLimit * 2))
                    .getContent();
                
                categoryProducts.stream()
                    .filter(p -> !p.getId().equals(productId) && !p.getIsVariation())
                    .forEach(similarProducts::add);
            }
            
            // Find products with same brand (limit to avoid too many results)
            if (product.getBrand() != null) {
                List<Product> brandProducts = productRepository.findByBrandId(product.getBrand().getId());
                brandProducts.stream()
                    .filter(p -> !p.getId().equals(productId) && !p.getIsVariation() && "published".equals(p.getStatus()))
                    .limit(recommendationLimit)
                    .forEach(p -> {
                        if (!similarProductIds.contains(p.getId())) {
                            similarProducts.add(p);
                            similarProductIds.add(p.getId());
                        }
                    });
            }
            
            // Find products with similar tags
            if (product.getTags() != null && !product.getTags().isEmpty()) {
                Set<Long> tagIds = product.getTags().stream()
                    .map(ProductTag::getId)
                    .collect(Collectors.toSet());
                
                // TODO: Add query to find products with similar tags
                // For now, this is a placeholder for tag-based recommendations
            }
            
            // Convert to ProductResponse and limit
            return similarProducts.stream()
                    .filter(p -> !p.getId().equals(productId))
                    .distinct()
                    .limit(recommendationLimit)
                    .map(p -> productCatalogService.getProduct(p.getId()))
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting similar products for product: {}", productId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get recently viewed products
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getRecentlyViewed(Long userId, Integer limit) {
        log.info("Getting recently viewed for user: {}", userId);
        
        if (userId == null) {
            log.warn("User ID is required for recently viewed");
            return new ArrayList<>();
        }
        
        int viewLimit = limit != null && limit > 0 ? limit : 10;
        
        try {
            // Use RecentlyViewedService to get recently viewed products
            List<CustomerRecentlyViewedProduct> recentlyViewed = recentlyViewedRepository
                    .findRecentByCustomerId(userId, PageRequest.of(0, viewLimit));
            
            return recentlyViewed.stream()
                    .map(rv -> productCatalogService.getProduct(rv.getProductId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting recently viewed for user: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Track user behavior for recommendations
     */
    @Transactional
    public void trackUserBehavior(Long userId, Long productId, String action) {
        log.info("Tracking user behavior: userId={}, productId={}, action={}", userId, productId, action);
        
        if (userId == null || productId == null || action == null) {
            log.warn("User ID, Product ID, and action are required for tracking");
            return;
        }
        
        try {
            // Track VIEW action via RecentlyViewedService
            if ("VIEW".equalsIgnoreCase(action)) {
                recentlyViewedService.addToRecentlyViewed(userId, productId);
            }
            
            // TODO: Store other behavior actions (ADD_TO_CART, PURCHASE, RATE) for ML model training
            // This could be stored in an analytics/behavior tracking table
            // Actions: VIEW, ADD_TO_CART, PURCHASE, RATE, REMOVE_FROM_CART, etc.
            
            log.debug("User behavior tracked: userId={}, productId={}, action={}", userId, productId, action);
            
        } catch (Exception e) {
            log.error("Error tracking user behavior: userId={}, productId={}, action={}", userId, productId, action, e);
        }
    }
    
    /**
     * Get content-based recommendations (based on user's recently viewed products)
     */
    private List<ProductResponse> getContentBasedRecommendations(Long userId, int limit) {
        try {
            // Get user's recently viewed products
            List<CustomerRecentlyViewedProduct> recentlyViewed = recentlyViewedRepository
                    .findRecentByCustomerId(userId, PageRequest.of(0, 5));
            
            if (recentlyViewed.isEmpty()) {
                return new ArrayList<>();
            }
            
            Set<Long> recommendedIds = new HashSet<>();
            
            // For each recently viewed product, find similar products
            for (CustomerRecentlyViewedProduct rv : recentlyViewed) {
                List<ProductResponse> similar = getSimilarProducts(rv.getProductId(), limit / recentlyViewed.size() + 1);
                similar.forEach(p -> recommendedIds.add(p.getId()));
            }
            
            // Get products
            return recommendedIds.stream()
                    .map(productCatalogService::getProduct)
                    .filter(Objects::nonNull)
                    .limit(limit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting content-based recommendations for user: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get collaborative filtering recommendations (based on similar users)
     * 
     * TODO: Implement full collaborative filtering
     * 1. Get user's purchase history
     * 2. Find users who purchased similar products
     * 3. Find products purchased by similar users but not by current user
     * 4. Rank by similarity score
     */
    private List<ProductResponse> getCollaborativeRecommendations(Long userId, int limit) {
        try {
            // TODO: Implement collaborative filtering
            // For now, return popular products as a placeholder
            // In production, would:
            // 1. Get user's purchase history from OrderRepository
            // 2. Find users with similar purchase patterns
            // 3. Recommend products liked by similar users
            // 4. Use cosine similarity or Jaccard similarity for user similarity
            
            return getPopularProducts(limit);
            
        } catch (Exception e) {
            log.error("Error getting collaborative recommendations for user: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get popular/trending products (based on reviews and sales)
     */
    @Cacheable(value = "recommendations", key = "'popular_' + #limit")
    @Transactional(readOnly = true)
    private List<ProductResponse> getPopularProducts(int limit) {
        try {
            // Get products with highest ratings and review counts
            List<Product> publishedProducts = productRepository.findByStatusAndIsVariationFalse(
                "published", PageRequest.of(0, limit * 2))
                .getContent();
            
            // Sort by rating and review count
            return publishedProducts.stream()
                    .sorted((p1, p2) -> {
                        Double rating1 = productReviewRepository.calculateAverageRating(p1.getId());
                        Double rating2 = productReviewRepository.calculateAverageRating(p2.getId());
                        Long count1 = productReviewRepository.countByProductId(p1.getId());
                        Long count2 = productReviewRepository.countByProductId(p2.getId());
                        
                        rating1 = rating1 != null ? rating1 : 0.0;
                        rating2 = rating2 != null ? rating2 : 0.0;
                        count1 = count1 != null ? count1 : 0L;
                        count2 = count2 != null ? count2 : 0L;
                        
                        // Sort by rating first, then by review count
                        int ratingCompare = Double.compare(rating2, rating1);
                        if (ratingCompare != 0) {
                            return ratingCompare;
                        }
                        return Long.compare(count2, count1);
                    })
                    .limit(limit)
                    .map(p -> productCatalogService.getProduct(p.getId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting popular products", e);
            return new ArrayList<>();
        }
    }
}
