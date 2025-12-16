package com.nexus.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ec_customer_used_coupons", indexes = {
    @Index(name = "idx_customer_used_coupons_discount_id", columnList = "discount_id"),
    @Index(name = "idx_customer_used_coupons_customer_id", columnList = "customer_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@IdClass(CustomerUsedCoupon.CustomerUsedCouponId.class)
public class CustomerUsedCoupon {
    
    @Id
    @Column(name = "discount_id", nullable = false)
    private Long discountId;
    
    @Id
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerUsedCouponId implements java.io.Serializable {
        private Long discountId;
        private Long customerId;
    }
}
