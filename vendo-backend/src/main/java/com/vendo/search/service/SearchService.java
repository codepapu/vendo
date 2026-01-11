package com.vendo.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vendo.product.entity.Category;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductTag;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductReviewRepository;
import com.vendo.search.document.ProductDocument;
import com.vendo.search.dto.response.SearchResponse;
import com.vendo.search.repository.ProductDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Search Service with Elasticsearch integration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductRepository productRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Search products with faceted search
     */
    @Transactional(readOnly = true)
    public SearchResponse searchProducts(
            String query,
            String category,
            Double minPrice,
            Double maxPrice,
            String brand,
            Double minRating,
            Integer page,
            Integer size) {
        
        log.info("Searching products: query={}, category={}, priceRange=[{},{}], brand={}, rating>={}", 
                query, category, minPrice, maxPrice, brand, minRating);
        
        try {
            int pageNumber = page != null ? page : 0;
            int pageSize = size != null ? size : 20;
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            
            Page<ProductDocument> results;
            
            // Build query based on filters
            if (query != null && !query.trim().isEmpty()) {
                // Text search
                results = productDocumentRepository.findByNameContainingOrDescriptionContaining(
                    query.trim(), query.trim(), pageable);
            } else {
                // Get all published products
                results = productDocumentRepository.findByStatus("published", pageable);
            }
            
            // Apply additional filters in memory (for now - can be moved to ES queries later)
            List<ProductDocument> filteredDocs = results.getContent().stream()
                    .filter(doc -> category == null || category.trim().isEmpty() || 
                            (doc.getCategories() != null && doc.getCategories().contains(category.trim())))
                    .filter(doc -> brand == null || brand.trim().isEmpty() || 
                            (doc.getBrand() != null && doc.getBrand().equals(brand.trim())))
                    .filter(doc -> minPrice == null || (doc.getPrice() != null && doc.getPrice() >= minPrice))
                    .filter(doc -> maxPrice == null || (doc.getPrice() != null && doc.getPrice() <= maxPrice))
                    .filter(doc -> minRating == null || (doc.getRating() != null && doc.getRating() >= minRating))
                    .collect(Collectors.toList());
            
            // Map results
            List<SearchResponse.ProductResult> products = filteredDocs.stream()
                    .map(this::mapToProductResult)
                    .collect(Collectors.toList());
            
            // Build facets (simplified - would need aggregations for full implementation)
            Map<String, List<SearchResponse.Facet>> facets = buildFacets(filteredDocs);
            
            // Get suggestions
            List<String> suggestions = getSearchSuggestions(query != null ? query : "", 5);
            
            return SearchResponse.builder()
                    .products(products)
                    .totalResults((long) filteredDocs.size())
                    .page(pageNumber)
                    .size(pageSize)
                    .facets(facets)
                    .suggestions(suggestions)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching products in Elasticsearch", e);
            // Return empty results on error
            return SearchResponse.builder()
                    .products(new ArrayList<>())
                    .totalResults(0L)
                    .page(page != null ? page : 0)
                    .size(size != null ? size : 20)
                    .facets(new HashMap<>())
                    .suggestions(new ArrayList<>())
                    .build();
        }
    }
    
    /**
     * Get search suggestions/autocomplete
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String query, Integer limit) {
        log.info("Getting search suggestions for: {}", query);
        
        try {
            if (query == null || query.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            int suggestionLimit = limit != null ? limit : 10;
            
            // Simple prefix search for suggestions using repository method
            List<ProductDocument> suggestions = productDocumentRepository.findByNameContaining(query.trim());
            
            return suggestions.stream()
                    .map(ProductDocument::getName)
                    .distinct()
                    .limit(suggestionLimit)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting search suggestions", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Index product in Elasticsearch
     */
    public void indexProduct(Long productId) {
        log.info("Indexing product: {}", productId);
        
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || product.getIsVariation()) {
                log.warn("Product {} not found or is a variation, skipping indexing", productId);
                return;
            }
            
            ProductDocument document = convertToDocument(product);
            productDocumentRepository.save(document);
            log.info("Successfully indexed product: {}", productId);
            
        } catch (Exception e) {
            log.error("Error indexing product: {}", productId, e);
        }
    }
    
    /**
     * Remove product from Elasticsearch index
     */
    public void removeProduct(Long productId) {
        log.info("Removing product from index: {}", productId);
        
        try {
            if (productDocumentRepository.existsById(productId)) {
                productDocumentRepository.deleteById(productId);
                log.info("Successfully removed product from index: {}", productId);
            }
            
        } catch (Exception e) {
            log.error("Error removing product from index: {}", productId, e);
        }
    }
    
    /**
     * Convert Product entity to ProductDocument
     */
    private ProductDocument convertToDocument(Product product) {
        // Get rating information
        Double averageRating = productReviewRepository.calculateAverageRating(product.getId());
        Long reviewCount = productReviewRepository.countByProductId(product.getId());
        
        // Extract first image URL
        String imageUrl = extractFirstImageUrl(product.getImages());
        
        // Get category names
        List<String> categoryNames = product.getCategories() != null ?
                product.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toList()) : new ArrayList<>();
        
        // Get primary category
        String primaryCategory = categoryNames.isEmpty() ? null : categoryNames.get(0);
        
        // Get tag names
        List<String> tagNames = product.getTags() != null ?
                product.getTags().stream()
                        .map(ProductTag::getName)
                        .collect(Collectors.toList()) : new ArrayList<>();
        
        return ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .imageUrl(imageUrl)
                .price(product.getPrice() != null ? product.getPrice().doubleValue() : null)
                .salePrice(product.getSalePrice() != null ? product.getSalePrice().doubleValue() : null)
                .rating(averageRating != null ? averageRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .category(primaryCategory)
                .categories(categoryNames)
                .brand(product.getBrand() != null ? product.getBrand().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .status(product.getStatus())
                .isFeatured(product.getIsFeatured())
                .sku(product.getSku())
                .quantity(product.getQuantity())
                .tags(tagNames)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    /**
     * Extract first image URL from images JSON string
     */
    private String extractFirstImageUrl(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Try to parse as JSON array
            List<String> imageUrls = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
            return imageUrls.isEmpty() ? null : imageUrls.get(0);
        } catch (Exception e) {
            // If not JSON, try as comma-separated
            try {
                String[] urls = imagesJson.split(",");
                return urls.length > 0 ? urls[0].trim() : null;
            } catch (Exception ex) {
                // If single URL string
                return imagesJson.trim();
            }
        }
    }
    
    /**
     * Map ProductDocument to ProductResult
     */
    private SearchResponse.ProductResult mapToProductResult(ProductDocument document) {
        return SearchResponse.ProductResult.builder()
                .id(document.getId())
                .name(document.getName())
                .slug(document.getSlug())
                .description(document.getDescription())
                .imageUrl(document.getImageUrl())
                .price(document.getPrice())
                .salePrice(document.getSalePrice())
                .rating(document.getRating())
                .reviewCount(document.getReviewCount() != null ? document.getReviewCount().intValue() : 0)
                .category(document.getCategory())
                .brand(document.getBrand())
                .build();
    }
    
    /**
     * Build facets from search results (simplified implementation)
     */
    private Map<String, List<SearchResponse.Facet>> buildFacets(List<ProductDocument> documents) {
        Map<String, List<SearchResponse.Facet>> facets = new HashMap<>();
        
        // Category facets
        Map<String, Long> categoryCounts = new HashMap<>();
        documents.forEach(doc -> {
            if (doc.getCategories() != null) {
                doc.getCategories().forEach(cat -> {
                    categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0L) + 1);
                });
            }
        });
        
        List<SearchResponse.Facet> categoryFacets = categoryCounts.entrySet().stream()
                .map(e -> SearchResponse.Facet.builder()
                        .value(e.getKey())
                        .count(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
        facets.put("categories", categoryFacets);
        
        // Brand facets
        Map<String, Long> brandCounts = new HashMap<>();
        documents.forEach(doc -> {
            if (doc.getBrand() != null) {
                brandCounts.put(doc.getBrand(), brandCounts.getOrDefault(doc.getBrand(), 0L) + 1);
            }
        });
        
        List<SearchResponse.Facet> brandFacets = brandCounts.entrySet().stream()
                .map(e -> SearchResponse.Facet.builder()
                        .value(e.getKey())
                        .count(e.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
        facets.put("brands", brandFacets);
        
        return facets;
    }
}
