package com.vendo.order.repository;

import com.vendo.order.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, Long> {
    
    Optional<ShippingMethod> findByCode(String code);
    
    List<ShippingMethod> findByIsActiveTrue();
    
    @Query("SELECT sm FROM ShippingMethod sm WHERE sm.isActive = true ORDER BY sm.baseCost ASC")
    List<ShippingMethod> findActiveShippingMethodsOrderedByCost();
    
    @Query("SELECT sm FROM ShippingMethod sm WHERE sm.isActive = true AND sm.code = :code")
    Optional<ShippingMethod> findActiveByCode(@Param("code") String code);
}

