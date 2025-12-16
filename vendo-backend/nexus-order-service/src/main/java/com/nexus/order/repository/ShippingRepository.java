package com.nexus.order.repository;

import com.nexus.order.entity.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Long> {
    
    List<Shipping> findByIsEnabledTrue();
    
    List<Shipping> findByCountry(String country);
}
