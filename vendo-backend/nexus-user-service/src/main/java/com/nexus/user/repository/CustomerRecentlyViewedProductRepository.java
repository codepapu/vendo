package com.nexus.user.repository;

import com.nexus.user.entity.CustomerRecentlyViewedProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRecentlyViewedProductRepository extends JpaRepository<CustomerRecentlyViewedProduct, Long> {
    
    List<CustomerRecentlyViewedProduct> findByCustomerId(Long customerId);
    
    @Query("SELECT r FROM CustomerRecentlyViewedProduct r WHERE r.customerId = :customerId ORDER BY r.createdAt DESC")
    List<CustomerRecentlyViewedProduct> findRecentByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT r FROM CustomerRecentlyViewedProduct r WHERE r.customerId = :customerId ORDER BY r.createdAt DESC LIMIT :limit")
    List<CustomerRecentlyViewedProduct> findRecentByCustomerIdLimit(@Param("customerId") Long customerId, @Param("limit") int limit);
}
