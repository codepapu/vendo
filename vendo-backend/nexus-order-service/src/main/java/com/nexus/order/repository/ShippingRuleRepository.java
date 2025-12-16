package com.nexus.order.repository;

import com.nexus.order.entity.ShippingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {
    
    List<ShippingRule> findByShippingId(Long shippingId);
    
    @Query("SELECT r FROM ShippingRule r WHERE r.shipping.id = :shippingId AND " +
           "(:value >= r.fromValue AND (r.toValue IS NULL OR :value <= r.toValue)) " +
           "ORDER BY r.fromValue ASC")
    Optional<ShippingRule> findApplicableRule(@Param("shippingId") Long shippingId, @Param("value") BigDecimal value);
}
