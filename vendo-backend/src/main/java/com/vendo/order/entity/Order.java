package com.vendo.order.entity;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ec_orders", indexes = {
    @Index(name = "idx_ec_orders_user_id", columnList = "user_id"),
    @Index(name = "idx_ec_orders_business_account_id", columnList = "business_account_id"),
    @Index(name = "idx_ec_orders_status", columnList = "status"),
    @Index(name = "idx_ec_orders_created_at", columnList = "created_at"),
    @Index(name = "idx_ec_orders_code", columnList = "code")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_ec_orders_code", columnNames = {"code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", unique = true, length = 191)
    @jakarta.validation.constraints.Size(max = 191)
    private String orderNumber; // mapped to ec_orders.code
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.vendo.user.entity.User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id")
    private com.vendo.b2b.entity.BusinessAccount businessAccount;
    
    @Column(name = "order_type", nullable = false, length = 20)
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 20)
    @Builder.Default
    private String orderType = "B2C"; // B2C, B2B, RETAILER
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    
    @Column(name = "payment_status", length = 50)
    @jakarta.validation.constraints.Size(max = 50)
    private String paymentStatus; // PENDING, PAID, PARTIAL, REFUNDED, FAILED
    
    @Column(name = "payment_method", length = 50)
    @jakarta.validation.constraints.Size(max = 50)
    private String paymentMethod;
    
    @Column(name = "sub_total", nullable = false, precision = 15, scale = 2)
    @jakarta.validation.constraints.NotNull
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "shipping_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    @jakarta.validation.constraints.NotNull
    private BigDecimal totalAmount; // mapped to ec_orders.amount
    
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";
    
    @Column(columnDefinition = "TEXT")
    private String notes; // ec_orders.notes added in V66
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    // Helper method to get userId from user relationship
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    // Helper method for code compatibility (returns orderNumber)
    public String getCode() {
        return orderNumber;
    }
    
    // Helper method for amount compatibility (returns totalAmount)
    public BigDecimal getAmount() {
        return totalAmount;
    }
    
    // Helper method for subtotal compatibility
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>(); // Keep for backward compatibility
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PurchaseOrder> purchaseOrders = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.product.entity.ProductReview> productReviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.product.entity.RetailerReview> retailerReviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderAddress> addresses = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderHistory> histories = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderReturn> returns = new ArrayList<>();
}
