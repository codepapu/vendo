package com.nexus.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_user_id", columnList = "user_id"),
    @Index(name = "idx_payments_order_id", columnList = "order_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_charge_id", columnList = "charge_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 120)
    private String currency;
    
    @Column(name = "user_id", nullable = false)
    @Builder.Default
    private Long userId = 0L;
    
    @Column(length = 191)
    private String chargeId;
    
    @Column(length = 60)
    private String paymentChannel;
    
    @Column(length = 400)
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paymentFee = BigDecimal.ZERO;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(length = 60)
    @Builder.Default
    private String status = "pending";
    
    @Column(length = 191)
    @Builder.Default
    private String paymentType = "confirm";
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal refundedAmount;
    
    @Column(length = 191)
    private String refundNote;
    
    @Column(length = 191)
    private String customerType;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
