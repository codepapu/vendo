package com.vendo.cart.repository;

import com.vendo.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.cartType = :cartType")
    Optional<Cart> findByUserIdAndCartType(@Param("userId") Long userId, @Param("cartType") String cartType);
    
    Optional<Cart> findByBusinessAccountId(Long businessAccountId);
    
    Optional<Cart> findBySessionId(String sessionId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND (c.cartType = :cartType OR c.cartType = 'MULTI_VENDOR')")
    Optional<Cart> findByUserIdAndCartTypeOrMultiVendor(@Param("userId") Long userId, @Param("cartType") String cartType);
}
