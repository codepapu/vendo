package com.vendo.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupons_code", columnList = "code"),
    @Index(name = "idx_coupons_is_active", columnList = "is_active"),
    @Index(name = "idx_coupons_valid_from", columnList = "valid_from"),
    @Index(name = "idx_coupons_valid_until", columnList = "valid_until")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_coupons_code", columnNames = {"code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank
    @Size(max = 50)
    private String code;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "discount_type", nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String discountType; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    @NotNull
    @Min(0)
    private BigDecimal discountValue;
    
    @Column(name = "minimum_purchase", precision = 10, scale = 2)
    @Min(0)
    private BigDecimal minimumPurchase;
    
    @Column(name = "maximum_discount", precision = 10, scale = 2)
    @Min(0)
    private BigDecimal maximumDiscount;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;
    
    @Column(name = "valid_from", nullable = false)
    @NotNull
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until", nullable = false)
    @NotNull
    private LocalDateTime validUntil;
    
    @Column(name = "applicable_to", length = 50)
    @Size(max = 50)
    private String applicableTo; // ALL, B2C, B2B, SPECIFIC_PRODUCTS, SPECIFIC_CATEGORIES
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "coupon_products",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private List<Product> products = new ArrayList<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "coupon_categories",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
    
    // Validation method to ensure valid_until > valid_from
    @jakarta.validation.constraints.AssertTrue(message = "valid_until must be after valid_from")
    private boolean isValidDateRange() {
        if (validFrom == null || validUntil == null) {
            return true; // Let @NotNull handle null validation
        }
        return validUntil.isAfter(validFrom);
    }
}
