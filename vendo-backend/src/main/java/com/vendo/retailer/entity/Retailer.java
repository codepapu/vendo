package com.vendo.retailer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "retailers", indexes = {
    @Index(name = "idx_retailers_user_id", columnList = "user_id"),
    @Index(name = "idx_retailers_status", columnList = "status"),
    @Index(name = "idx_retailers_store_slug", columnList = "store_slug")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_retailers_store_slug", columnNames = {"store_slug"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Retailer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private com.vendo.user.entity.User user;
    
    @Column(name = "store_name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String storeName;
    
    @Column(name = "store_slug", nullable = false, unique = true, length = 255)
    @NotBlank
    @Size(max = 255)
    private String storeSlug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "logo_url", length = 500)
    @Size(max = 500)
    private String logoUrl;
    
    @Column(name = "banner_url", length = 500)
    @Size(max = 500)
    private String bannerUrl;
    
    @Column(name = "commission_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal commissionRate = BigDecimal.ZERO;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, ACTIVE, SUSPENDED, REJECTED
    
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_sales", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalSales = BigDecimal.ZERO;
    
    @Column(name = "total_orders", nullable = false)
    @Builder.Default
    private Integer totalOrders = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean verified = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "retailer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RetailerBankAccount> bankAccounts = new ArrayList<>();
    
    @OneToMany(mappedBy = "retailer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.product.entity.ProductRetailer> products = new ArrayList<>();
    
    @OneToMany(mappedBy = "retailer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.product.entity.RetailerReview> reviews = new ArrayList<>();
}
