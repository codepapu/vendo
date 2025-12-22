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
@Table(name = "ec_order_products", indexes = {
    @Index(name = "idx_order_products_order_id", columnList = "order_id"),
    @Index(name = "idx_order_products_product_id", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "product_id")
    private Long productId;
    
    @Column(nullable = false, length = 191)
    private String productName;
    
    @Column(length = 191)
    private String productImage;
    
    @Column(nullable = false)
    private Integer qty;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String options;
    
    @Column(columnDefinition = "TEXT")
    private String productOptions;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
