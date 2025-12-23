package com.vendo.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "b2b_pricing_tiers", indexes = {
    @Index(name = "idx_b2b_pricing_tiers_product_id", columnList = "product_id"),
    @Index(name = "idx_b2b_pricing_tiers_business_type", columnList = "business_type"),
    @Index(name = "idx_b2b_pricing_tiers_is_active", columnList = "is_active"),
    @Index(name = "idx_b2b_pricing_tiers_product_business_type", columnList = "product_id, business_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class B2BPricingTier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;
    
    @Column(name = "business_type", length = 50)
    @Size(max = 50)
    private String businessType; // CORPORATE, SME, ENTERPRISE
    
    @Column(name = "min_quantity", nullable = false)
    @NotNull
    @Min(1)
    private Integer minQuantity;
    
    @Column(name = "max_quantity")
    private Integer maxQuantity; // Optional - if provided, should be >= minQuantity (enforced by DB constraint)
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @jakarta.validation.constraints.DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
