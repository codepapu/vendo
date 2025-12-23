package com.vendo.product.repository;

import com.vendo.product.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND " +
           "c.validFrom <= :now AND c.validUntil >= :now AND " +
           "(c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true AND " +
           "c.validFrom <= :now AND c.validUntil >= :now AND " +
           "(c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    Optional<Coupon> findValidCouponByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true")
    List<Coupon> findAllActive();
    
    @Query("SELECT c FROM Coupon c WHERE c.applicableTo = :applicableTo OR c.applicableTo = 'ALL'")
    List<Coupon> findByApplicableTo(@Param("applicableTo") String applicableTo);
}
