package com.vendo.b2b.entity;

import com.vendo.order.entity.Order;
import com.vendo.product.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "bulk_order_items", indexes = {
    @Index(name = "idx_bulk_order_items_bulk_order_id", columnList = "bulk_order_id"),
    @Index(name = "idx_bulk_order_items_product_id", columnList = "product_id"),
    @Index(name = "idx_bulk_order_items_status", columnList = "status"),
    @Index(name = "idx_bulk_order_items_order_id", columnList = "order_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BulkOrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulk_order_id", nullable = false)
    @NotNull
    private BulkOrder bulkOrder;
    
    @Column(name = "row_number", nullable = false)
    @NotNull
    @Positive
    private Integer rowNumber; // row number in original file
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(name = "product_sku", length = 100)
    private String productSku;
    
    @Column(name = "product_name", length = 255)
    private String productName;
    
    @Column(nullable = false)
    @NotNull
    @Positive
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // created order if successful
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, VALIDATED, PROCESSED, FAILED
    
    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors; // validation errors for this item
    
    @Column(name = "processing_errors", columnDefinition = "TEXT")
    private String processingErrors; // processing errors for this item
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

