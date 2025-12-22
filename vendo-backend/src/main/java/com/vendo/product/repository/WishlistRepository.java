package com.vendo.product.repository;

import com.vendo.product.entity.Wishlist;
import com.vendo.product.entity.Wishlist.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    
    List<Wishlist> findByCustomerId(Long customerId);
    
    List<Wishlist> findByProductId(Long productId);
    
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
    
    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.customerId = :customerId AND w.productId = :productId")
    void deleteByCustomerIdAndProductId(@Param("customerId") Long customerId, @Param("productId") Long productId);
}
