package com.vendo.product.repository;

import com.vendo.product.entity.Product;
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
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isVariation = false ORDER BY p.order ASC")
    List<Product> findPublishedProducts(@Param("status") String status);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isFeatured = true AND p.isVariation = false")
    List<Product> findFeaturedProducts(@Param("status") String status);
    
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId AND p.status = :status AND p.isVariation = false")
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Long categoryId, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.status = :status AND p.isVariation = false")
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
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isVariation = false AND " +
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
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isVariation = false AND " +
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
}
