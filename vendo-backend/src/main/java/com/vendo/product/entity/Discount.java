package com.vendo.product.entity;

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
@Table(name = "ec_discounts", indexes = {
    @Index(name = "idx_discounts_code", columnList = "code"),
    @Index(name = "idx_discounts_status", columnList = "status"),
    @Index(name = "idx_discounts_start_date", columnList = "start_date"),
    @Index(name = "idx_discounts_end_date", columnList = "end_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Discount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 120)
    private String title;
    
    @Column(length = 20, unique = true)
    private String code;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Integer quantity;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer totalUsed = 0;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal value;
    
    @Column(length = 60)
    @Builder.Default
    private String type = "coupon";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean canUseWithPromotion = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean canUseWithFlashSale = false;
    
    @Column(length = 20)
    private String discountOn;
    
    private Integer productQuantity;
    
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String typeOption = "amount";
    
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String target = "all-orders";
    
    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderPrice;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean applyViaUrl = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean displayAtCheckout = false;
    
    private Long storeId;
    
    @Column(length = 400)
    private String description;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToMany
    @JoinTable(
        name = "ec_discount_products",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private List<Product> products = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "ec_discount_product_categories",
        joinColumns = @JoinColumn(name = "discount_id"),
        inverseJoinColumns = @JoinColumn(name = "product_category_id")
    )
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "ec_discount_customers", joinColumns = @JoinColumn(name = "discount_id"))
    @Column(name = "customer_id")
    @Builder.Default
    private List<Long> customerIds = new ArrayList<>();
}
