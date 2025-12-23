package com.vendo.user.repository;

import com.vendo.user.entity.CustomerRecentlyViewedProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRecentlyViewedProductRepository extends JpaRepository<CustomerRecentlyViewedProduct, Long> {
    
    List<CustomerRecentlyViewedProduct> findByCustomerId(Long customerId);
    
    @Query("SELECT r FROM CustomerRecentlyViewedProduct r WHERE r.customerId = :customerId ORDER BY r.createdAt DESC")
    List<CustomerRecentlyViewedProduct> findRecentByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
    
    @Query("SELECT r FROM CustomerRecentlyViewedProduct r WHERE r.customerId = :customerId ORDER BY r.createdAt DESC")
    List<CustomerRecentlyViewedProduct> findRecentByCustomerId(@Param("customerId") Long customerId);
    
    Optional<CustomerRecentlyViewedProduct> findByCustomerIdAndProductId(Long customerId, Long productId);
    
    @Modifying
    @Query("DELETE FROM CustomerRecentlyViewedProduct r WHERE r.customerId = :customerId")
    void deleteByCustomerId(@Param("customerId") Long customerId);
}
