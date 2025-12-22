package com.vendo.order.entity;

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
@Table(name = "ec_shipment_histories", indexes = {
    @Index(name = "idx_shipment_histories_shipment_id", columnList = "shipment_id"),
    @Index(name = "idx_shipment_histories_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShipmentHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(nullable = false, length = 120)
    private String action;
    
    @Column(length = 400)
    private String description;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, length = 191)
    @Builder.Default
    private String userType = "User";
    
    @Column(columnDefinition = "TEXT")
    private String extras;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
