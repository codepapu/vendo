package com.vendo.payment.entity;

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
    @Index(name = "idx_payments_order_id", columnList = "order_id"),
    @Index(name = "idx_payments_user_id", columnList = "user_id"),
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private com.vendo.order.entity.Order order;
    
    @Column(name = "order_id", insertable = false, updatable = false)
    private Long orderId;
    
    @Column(name = "user_id", nullable = false)
    @Builder.Default
    private Long userId = 0L;
    
    @Column(name = "charge_id", length = 191)
    private String chargeId;
    
    @Column(name = "payment_channel", length = 60)
    private String paymentChannel;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "payment_gateway", length = 50)
    private String paymentGateway;
    
    @Column(name = "transaction_id", length = 255)
    private String transactionId;
    
    @Column(name = "description", length = 400)
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @jakarta.validation.constraints.NotNull
    private BigDecimal amount;
    
    @Column(name = "payment_fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paymentFee = BigDecimal.ZERO;
    
    @Column(length = 120)
    @Builder.Default
    private String currency = "USD";
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "pending"; // pending, completed, failed, refunded, partially_refunded
    
    @Column(name = "payment_type", length = 191)
    @Builder.Default
    private String paymentType = "confirm";
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "refunded_amount", precision = 15, scale = 2)
    private BigDecimal refundedAmount;
    
    @Column(name = "refund_note", length = 191)
    private String refundNote;
    
    @Column(name = "customer_type", length = 191)
    private String customerType;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "gateway_response", columnDefinition = "JSONB")
    private String gatewayResponse;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
