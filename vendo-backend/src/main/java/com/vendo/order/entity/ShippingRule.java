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
@Table(name = "ec_shipping_rules", indexes = {
    @Index(name = "idx_shipping_rules_shipping_id", columnList = "shipping_id"),
    @Index(name = "idx_shipping_rules_type", columnList = "type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShippingRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_id", nullable = false)
    private Shipping shipping;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String type = "base_on_price";
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fromValue = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal toValue;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
