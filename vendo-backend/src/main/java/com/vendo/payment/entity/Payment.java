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
    @Index(name = "idx_payments_invoice_id", columnList = "invoice_id"),
    @Index(name = "idx_payments_status", columnList = "status")
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private com.vendo.order.entity.Invoice invoice;
    
    @Column(name = "payment_method", nullable = false, length = 50)
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 50)
    private String paymentMethod;
    
    @Column(name = "payment_gateway", length = 50)
    @jakarta.validation.constraints.Size(max = 50)
    private String paymentGateway;
    
    @Column(name = "transaction_id", length = 255)
    @jakarta.validation.constraints.Size(max = 255)
    private String transactionId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @jakarta.validation.constraints.NotNull
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, COMPLETED, FAILED, REFUNDED
    
    @Column(name = "gateway_response", columnDefinition = "JSONB")
    private String gatewayResponse;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
