package com.nexus.order.entity;

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
    @Index(name = "idx_orders_user_status_created", columnList = "user_id,status,created_at"),
    @Index(name = "idx_orders_code", columnList = "code"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_store_id", columnList = "store_id")
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
    
    @Column(length = 191, unique = true)
    private String code;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(length = 60)
    private String shippingOption;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String shippingMethod = "default";
    
    @Column(nullable = false, length = 120)
    @Builder.Default
    private String status = "pending";
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal shippingAmount;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paymentFee = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 120)
    private String couponCode;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subTotal;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isConfirmed = false;
    
    @Column(length = 191)
    private String discountDescription;
    
    @Builder.Default
    private Boolean isFinished = false;
    
    @Column(length = 191)
    private String cancellationReason;
    
    @Column(length = 191)
    private String cancellationReasonDescription;
    
    private LocalDateTime completedAt;
    
    @Column(length = 120)
    private String token;
    
    private Long paymentId;
    
    private Long storeId;
    
    @Column(length = 191)
    private String proofFile;
    
    @Column(columnDefinition = "TEXT")
    private String privateNotes;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderProduct> orderProducts = new ArrayList<>();
    
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
