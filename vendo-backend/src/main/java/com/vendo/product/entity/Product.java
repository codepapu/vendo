package com.vendo.product.entity;

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
@Table(name = "products", indexes = {
    @Index(name = "idx_products_slug", columnList = "slug"),
    @Index(name = "idx_products_status", columnList = "status"),
    @Index(name = "idx_products_sku", columnList = "sku"),
    @Index(name = "idx_products_is_featured", columnList = "is_featured")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_products_slug", columnNames = {"slug"}),
    @UniqueConstraint(name = "uk_products_sku", columnNames = {"sku"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;
    
    @Column(nullable = false, unique = true, length = 255)
    @NotBlank
    @Size(max = 255)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "published";
    
    @Column(columnDefinition = "TEXT")
    private String images;
    
    @Column(columnDefinition = "TEXT")
    private String videoMedia;
    
    @Column(nullable = false, unique = true, length = 100)
    @NotBlank
    @Size(max = 100)
    private String sku;
    
    @Column
    private Integer quantity;
    
    @Column(name = "allow_checkout_when_out_of_stock", nullable = false)
    @Builder.Default
    private Boolean allowCheckoutWhenOutOfStock = false;
    
    @Column(name = "with_storehouse_management", nullable = false)
    @Builder.Default
    private Boolean withStorehouseManagement = false;
    
    @Column(name = "stock_status", length = 191)
    @Builder.Default
    private String stockStatus = "in_stock";
    
    @Column
    @Builder.Default
    private Integer order = 0;
    
    @Column(length = 100)
    @Size(max = 100)
    private String barcode;
    
    @Column(name = "product_type", length = 50)
    @Size(max = 50)
    @Builder.Default
    private String productType = "SIMPLE";
    
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;
    
    @Column(columnDefinition = "JSONB")
    private String dimensions; // JSON: {length, width, height, unit}
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVariation = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer saleType = 0;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal price;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal salePrice;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal length;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal wide;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal height;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal weight;
    
    private Long taxId;
    
    private Long storeId;
    
    @Column(nullable = false)
    @Builder.Default
    private Long createdById = 0L;
    
    @Column(nullable = false, length = 191)
    @Builder.Default
    private String createdByType = "User";
    
    @Builder.Default
    private Long approvedBy = 0L;
    
    @Column(length = 191)
    private String image;
    
    
    @Column(precision = 15, scale = 2)
    private BigDecimal costPerItem;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean generateLicenseCode = false;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String licenseCodeType = "auto_generate";
    
    @Builder.Default
    private Integer minimumOrderQuantity = 0;
    
    @Builder.Default
    private Integer maximumOrderQuantity = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean notifyAttachmentUpdated = false;
    
    private Long specificationTableId;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToMany
    @JoinTable(
        name = "product_category_product",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private List<Category> categories = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "ec_product_tag_product",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private List<ProductTag> tags = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductVariation> variations = new ArrayList<>();
    
    @OneToMany(mappedBy = "configurableProduct", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductVariation> configurableVariations = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductReview> reviews = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductRetailer> productRetailers = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<B2BPricingTier> b2bPricingTiers = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<VolumeDiscount> volumeDiscounts = new ArrayList<>();
    
    @ManyToMany(mappedBy = "products")
    @Builder.Default
    private List<Tax> taxes = new ArrayList<>();
    
    @ManyToMany(mappedBy = "products")
    @Builder.Default
    private List<ProductCollection> collections = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductFile> files = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductView> views = new ArrayList<>();
}
