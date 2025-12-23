package com.vendo.product.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.response.ProductResponse;
import com.vendo.product.entity.Category;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductCollection;
import com.vendo.product.entity.ProductTag;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for product catalog operations with filtering, sorting, and pagination support.
 * Provides methods for browsing products, searching, and retrieving featured products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCatalogService {
    
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    
    /**
     * Get paginated list of products with filtering and sorting support.
     * 
     * @param status Product status filter (default: "published")
     * @param categoryId Filter by category ID (optional)
     * @param brandId Filter by brand ID (optional)
     * @param minPrice Minimum price filter (optional)
     * @param maxPrice Maximum price filter (optional)
     * @param minRating Minimum average rating filter (optional, 1-5)
     * @param sortBy Field to sort by: price, name, rating, createdAt (default: createdAt)
     * @param sortDir Sort direction: ASC or DESC (default: DESC)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of ProductResponse
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            String status,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            String sortBy,
            Sort.Direction sortDir,
            int page,
            int size) {
        
        // Default status to published if not provided
        String productStatus = (status != null && !status.isEmpty()) ? status : "published";
        
        // Build sort criteria
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        // Use filtered query if any filters are provided
        Page<Product> products;
        if (categoryId != null || brandId != null || minPrice != null || maxPrice != null || minRating != null) {
            products = productRepository.findProductsWithFilters(
                    productStatus, categoryId, brandId, minPrice, maxPrice, minRating, pageable);
        } else {
            products = productRepository.findByStatusAndIsVariationFalse(productStatus, pageable);
        }
        
        return products.map(this::mapToResponseWithRating);
    }
    
    /**
     * Get a single product by ID with rating information.
     * 
     * @param id Product ID
     * @return ProductResponse with rating details
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponseWithRating(product);
    }
    
    /**
     * Search products with keyword and filters.
     * 
     * @param keyword Search keyword
     * @param status Product status filter (default: "published")
     * @param categoryId Filter by category ID (optional)
     * @param brandId Filter by brand ID (optional)
     * @param minPrice Minimum price filter (optional)
     * @param maxPrice Maximum price filter (optional)
     * @param minRating Minimum average rating filter (optional, 1-5)
     * @param sortBy Field to sort by: price, name, rating, createdAt (default: createdAt)
     * @param sortDir Sort direction: ASC or DESC (default: DESC)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of ProductResponse
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String keyword,
            String status,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            String sortBy,
            Sort.Direction sortDir,
            int page,
            int size) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            // If no keyword, fall back to getProducts
            return getProducts(status, categoryId, brandId, minPrice, maxPrice, minRating, sortBy, sortDir, page, size);
        }
        
        String productStatus = (status != null && !status.isEmpty()) ? status : "published";
        
        // Build sort criteria
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        // Use filtered search query if any filters are provided
        Page<Product> products;
        if (categoryId != null || brandId != null || minPrice != null || maxPrice != null || minRating != null) {
            products = productRepository.searchProductsWithFilters(
                    productStatus, keyword.trim(), categoryId, brandId, minPrice, maxPrice, minRating, pageable);
        } else {
            products = productRepository.searchProducts(productStatus, keyword.trim(), pageable);
        }
        
        return products.map(this::mapToResponseWithRating);
    }
    
    /**
     * Get products by category with filtering and sorting.
     * 
     * @param categoryId Category ID
     * @param status Product status filter (default: "published")
     * @param brandId Filter by brand ID (optional)
     * @param minPrice Minimum price filter (optional)
     * @param maxPrice Maximum price filter (optional)
     * @param minRating Minimum average rating filter (optional, 1-5)
     * @param sortBy Field to sort by: price, name, rating, createdAt (default: createdAt)
     * @param sortDir Sort direction: ASC or DESC (default: DESC)
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of ProductResponse
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(
            Long categoryId,
            String status,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double minRating,
            String sortBy,
            Sort.Direction sortDir,
            int page,
            int size) {
        
        String productStatus = (status != null && !status.isEmpty()) ? status : "published";
        
        // Build sort criteria
        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        // Use filtered query
        Page<Product> products = productRepository.findProductsWithFilters(
                productStatus, categoryId, brandId, minPrice, maxPrice, minRating, pageable);
        
        return products.map(this::mapToResponseWithRating);
    }
    
    /**
     * Get featured products.
     * 
     * @param status Product status filter (default: "published")
     * @param limit Maximum number of products to return (optional, default: all)
     * @return List of ProductResponse
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(String status, Integer limit) {
        String productStatus = (status != null && !status.isEmpty()) ? status : "published";
        List<Product> products = productRepository.findFeaturedProducts(productStatus);
        
        List<ProductResponse> responses = products.stream()
                .map(this::mapToResponseWithRating)
                .collect(Collectors.toList());
        
        // Apply limit if specified
        if (limit != null && limit > 0 && responses.size() > limit) {
            return responses.subList(0, limit);
        }
        
        return responses;
    }
    
    /**
     * Build Sort object based on sortBy field and direction.
     * Supports sorting by: price, name, rating, createdAt
     * 
     * @param sortBy Field to sort by
     * @param sortDir Sort direction
     * @return Sort object
     */
    private Sort buildSort(String sortBy, Sort.Direction sortDir) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "createdAt";
        }
        
        if (sortDir == null) {
            sortDir = Sort.Direction.DESC;
        }
        
        // Map sortBy to actual entity field names
        String fieldName;
        switch (sortBy.toLowerCase()) {
            case "price":
                // Sort by salePrice if available, otherwise price
                // Note: This requires a custom comparator or we sort by price field
                fieldName = "price";
                break;
            case "name":
                fieldName = "name";
                break;
            case "rating":
                // Rating sorting requires custom query, so we'll sort by createdAt as fallback
                // and handle rating sorting in the query itself
                fieldName = "createdAt";
                break;
            case "date":
            case "createdat":
            case "created_at":
                fieldName = "createdAt";
                break;
            default:
                fieldName = "createdAt";
        }
        
        return Sort.by(sortDir, fieldName);
    }
    
    /**
     * Map Product entity to ProductResponse with rating information.
     * 
     * @param product Product entity
     * @return ProductResponse with rating details
     */
    private ProductResponse mapToResponseWithRating(Product product) {
        // Get rating information
        Double averageRating = productReviewRepository.calculateAverageRating(product.getId());
        Long reviewCount = productReviewRepository.countByProductId(product.getId());
        
        // Map product to response - only include fields that exist in Product entity
        ProductResponse response = ProductResponse.builder()
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
                        product.getCollections().stream().map(ProductCollection::getId).collect(Collectors.toList()) : null)
                .collectionNames(product.getCollections() != null ? 
                        product.getCollections().stream().map(ProductCollection::getName).collect(Collectors.toList()) : null)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
        
        return response;
    }
}
