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
@Table(name = "ec_order_return_items", indexes = {
    @Index(name = "idx_order_return_items_return_id", columnList = "return_id"),
    @Index(name = "idx_order_return_items_product_id", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderReturnItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private OrderReturn orderReturn;
    
    @Column(name = "order_product_id")
    private Long orderProductId;
    
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
    
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
