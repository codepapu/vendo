package com.vendo.b2b.entity;

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
@Table(name = "price_list_items", indexes = {
    @Index(name = "idx_price_list_items_price_list_id", columnList = "price_list_id"),
    @Index(name = "idx_price_list_items_product_id", columnList = "product_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_price_list_items_unique", columnNames = {"price_list_id", "product_id", "min_quantity"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PriceListItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    @NotNull
    private PriceList priceList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @Positive
    private BigDecimal price;
    
    @Column(name = "min_quantity", nullable = false)
    @NotNull
    @Positive
    @Builder.Default
    private Integer minQuantity = 1;
    
    @Column(name = "max_quantity")
    private Integer maxQuantity;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

