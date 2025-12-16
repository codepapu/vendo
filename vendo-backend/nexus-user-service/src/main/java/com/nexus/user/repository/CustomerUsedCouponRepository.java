package com.nexus.user.repository;

import com.nexus.user.entity.CustomerUsedCoupon;
import com.nexus.user.entity.CustomerUsedCoupon.CustomerUsedCouponId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerUsedCouponRepository extends JpaRepository<CustomerUsedCoupon, CustomerUsedCouponId> {
    
    List<CustomerUsedCoupon> findByCustomerId(Long customerId);
    
    List<CustomerUsedCoupon> findByDiscountId(Long discountId);
    
    boolean existsByDiscountIdAndCustomerId(Long discountId, Long customerId);
}
