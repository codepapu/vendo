package com.vendo.cart.repository;

import com.vendo.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByCartId(Long cartId);
    
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    
    Optional<CartItem> findByCartIdAndProductIdAndRetailerId(Long cartId, Long productId, Long retailerId);
    
    void deleteByCartId(Long cartId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.retailer.id = :retailerId")
    List<CartItem> findByCartIdAndRetailerId(@Param("cartId") Long cartId, @Param("retailerId") Long retailerId);
    
    @Query("SELECT DISTINCT ci.retailer.id FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.retailer.id IS NOT NULL")
    List<Long> findDistinctRetailerIdsByCartId(@Param("cartId") Long cartId);
}

