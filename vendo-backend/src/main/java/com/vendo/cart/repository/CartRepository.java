package com.vendo.cart.repository;

import com.vendo.cart.entity.Cart;
import com.vendo.cart.entity.Cart.CartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, CartId> {
    
    Optional<Cart> findByIdentifierAndInstance(String identifier, String instance);
    
    List<Cart> findByIdentifier(String identifier);
    
    void deleteByIdentifierAndInstance(String identifier, String instance);
}
