package com.vendo.product.repository;

import com.vendo.product.entity.Product;
import com.vendo.product.projection.ProductBasicInfoProjection;
import com.vendo.product.projection.ProductSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    List<Product> findByStatus(String status);
    
    List<Product> findByIsFeaturedTrue();
    
    List<Product> findByBrandId(Long brandId);
    
    List<Product> findByIsVariationFalse();
    
    Page<Product> findByStatus(String status, Pageable pageable);
    
    Page<Product> findByStatusAndIsVariationFalse(String status, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.brand WHERE p.status = :status AND p.isVariation = false ORDER BY p.order ASC")
    List<Product> findPublishedProducts(@Param("status") String status);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.brand WHERE p.status = :status AND p.isFeatured = true AND p.isVariation = false")
    List<Product> findFeaturedProducts(@Param("status") String status);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories c LEFT JOIN FETCH p.brand WHERE c.id = :categoryId AND p.status = :status AND p.isVariation = false")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.brand b WHERE b.id = :brandId AND p.status = :status AND p.isVariation = false")
    Page<Product> findByBrandIdAndStatus(@Param("brandId") Long brandId, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isVariation = false AND " +
           "(:minPrice IS NULL OR p.salePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.salePrice <= :maxPrice)")
    Page<Product> findByPriceRange(@Param("status") String status, 
                                    @Param("minPrice") BigDecimal minPrice, 
                                    @Param("maxPrice") BigDecimal maxPrice, 
                                    Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isVariation = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("status") String status, @Param("keyword") String keyword, Pageable pageable);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status AND p.isVariation = false")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.brand WHERE p.status = :status AND p.isVariation = false AND " +
           "(:categoryId IS NULL OR EXISTS (SELECT 1 FROM p.categories c WHERE c.id = :categoryId)) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:minPrice IS NULL OR COALESCE(p.salePrice, p.price) >= :minPrice) AND " +
           "(:maxPrice IS NULL OR COALESCE(p.salePrice, p.price) <= :maxPrice) AND " +
           "(:minRating IS NULL OR (SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true) >= :minRating)")
    Page<Product> findProductsWithFilters(@Param("status") String status,
                                          @Param("categoryId") Long categoryId,
                                          @Param("brandId") Long brandId,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice,
                                          @Param("minRating") Double minRating,
                                          Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.brand WHERE p.status = :status AND p.isVariation = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR EXISTS (SELECT 1 FROM p.categories c WHERE c.id = :categoryId)) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:minPrice IS NULL OR COALESCE(p.salePrice, p.price) >= :minPrice) AND " +
           "(:maxPrice IS NULL OR COALESCE(p.salePrice, p.price) <= :maxPrice) AND " +
           "(:minRating IS NULL OR (SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true) >= :minRating)")
    Page<Product> searchProductsWithFilters(@Param("status") String status,
                                            @Param("keyword") String keyword,
                                            @Param("categoryId") Long categoryId,
                                            @Param("brandId") Long brandId,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            @Param("minRating") Double minRating,
                                            Pageable pageable);
    
    // ============================================================================
    // Projection-based queries for reduced data transfer
    // ============================================================================
    
    /**
     * Find product summaries using projection to reduce data transfer.
     * Only selects essential fields for product listings.
     * Uses JPQL with constructor expression for better type safety.
     */
    @Query("SELECT new com.vendo.product.projection.ProductSummaryProjectionImpl(" +
           "p.id, p.name, p.description, p.status, p.images, p.sku, p.isFeatured, " +
           "b.id, b.name, p.price, p.salePrice, " +
           "(SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true), " +
           "(SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true), " +
           "p.createdAt) " +
           "FROM Product p LEFT JOIN p.brand b " +
           "WHERE p.status = :status AND p.isVariation = false")
    Page<ProductSummaryProjection> findProductSummaries(@Param("status") String status, Pageable pageable);
    
    /**
     * Find product summaries with filters using projection.
     * Reduces data transfer by only selecting necessary columns.
     */
    @Query("SELECT DISTINCT new com.vendo.product.projection.ProductSummaryProjectionImpl(" +
           "p.id, p.name, p.description, p.status, p.images, p.sku, p.isFeatured, " +
           "b.id, b.name, p.price, p.salePrice, " +
           "(SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true), " +
           "(SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true), " +
           "p.createdAt) " +
           "FROM Product p LEFT JOIN p.brand b " +
           "WHERE p.status = :status AND p.isVariation = false AND " +
           "(:categoryId IS NULL OR EXISTS (SELECT 1 FROM p.categories c WHERE c.id = :categoryId)) AND " +
           "(:brandId IS NULL OR b.id = :brandId) AND " +
           "(:minPrice IS NULL OR COALESCE(p.salePrice, p.price) >= :minPrice) AND " +
           "(:maxPrice IS NULL OR COALESCE(p.salePrice, p.price) <= :maxPrice) AND " +
           "(:minRating IS NULL OR (SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true) >= :minRating)")
    Page<ProductSummaryProjection> findProductSummariesWithFilters(
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable);
    
    /**
     * Find basic product info using projection for lightweight queries.
     * Only selects minimal fields needed for basic product information.
     */
    @Query("SELECT new com.vendo.product.projection.ProductBasicInfoProjectionImpl(" +
           "p.id, p.name, p.sku, p.price, p.salePrice, p.status, p.isFeatured) " +
           "FROM Product p WHERE p.id = :id")
    Optional<ProductBasicInfoProjection> findBasicInfoById(@Param("id") Long id);
    
    /**
     * Find featured product summaries using projection.
     * Reduces data transfer for featured product listings.
     */
    @Query("SELECT new com.vendo.product.projection.ProductSummaryProjectionImpl(" +
           "p.id, p.name, p.description, p.status, p.images, p.sku, p.isFeatured, " +
           "b.id, b.name, p.price, p.salePrice, " +
           "(SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true), " +
           "(SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = p.id AND r.isApproved = true), " +
           "p.createdAt) " +
           "FROM Product p LEFT JOIN p.brand b " +
           "WHERE p.status = :status AND p.isFeatured = true AND p.isVariation = false " +
           "ORDER BY p.createdAt DESC")
    List<ProductSummaryProjection> findFeaturedProductSummaries(@Param("status") String status);
}
