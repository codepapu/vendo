package com.vendo.retailer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_tracking", indexes = {
    @Index(name = "idx_commission_tracking_retailer_id", columnList = "retailer_id"),
    @Index(name = "idx_commission_tracking_order_id", columnList = "order_id"),
    @Index(name = "idx_commission_tracking_order_item_id", columnList = "order_item_id"),
    @Index(name = "idx_commission_tracking_status", columnList = "status"),
    @Index(name = "idx_commission_tracking_settlement_date", columnList = "settlement_date"),
    @Index(name = "idx_commission_tracking_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CommissionTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    @NotNull
    private Retailer retailer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private com.vendo.order.entity.Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private com.vendo.order.entity.OrderItem orderItem;
    
    @Column(name = "commission_amount", nullable = false, precision = 15, scale = 2)
    @NotNull
    @DecimalMin(value = "0.0", message = "Commission amount must be non-negative")
    private BigDecimal commissionAmount;
    
    @Column(name = "commission_rate", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Commission rate must be non-negative")
    @DecimalMax(value = "100.0", message = "Commission rate cannot exceed 100")
    private BigDecimal commissionRate;
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, SETTLED, CANCELLED
    
    @Column(name = "settlement_date")
    private LocalDate settlementDate;
    
    @Column(name = "payment_reference", length = 255)
    private String paymentReference;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

