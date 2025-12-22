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

@Entity
@Table(name = "ec_shipments", indexes = {
    @Index(name = "idx_shipments_order_id", columnList = "order_id"),
    @Index(name = "idx_shipments_status", columnList = "status"),
    @Index(name = "idx_shipments_tracking_id", columnList = "tracking_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal weight = BigDecimal.ZERO;
    
    @Column(length = 120)
    private String shipmentId;
    
    @Column(length = 120)
    private String rateId;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(nullable = false, length = 120)
    @Builder.Default
    private String status = "pending";
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal codAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String codStatus = "pending";
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String crossCheckingStatus = "pending";
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
    
    private Long storeId;
    
    @Column(length = 191)
    private String trackingId;
    
    @Column(length = 191)
    private String shippingCompanyName;
    
    @Column(length = 191)
    private String trackingLink;
    
    private LocalDateTime estimateDateShipped;
    
    private LocalDateTime dateShipped;
    
    private LocalDateTime customerDeliveredConfirmedAt;
    
    @Column(columnDefinition = "TEXT")
    private String labelUrl;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShipmentHistory> histories = new ArrayList<>();
}
