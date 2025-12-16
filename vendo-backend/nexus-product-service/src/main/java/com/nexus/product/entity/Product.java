package com.nexus.product.entity;

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
@Table(name = "ec_products", indexes = {
    @Index(name = "idx_products_brand_status_variation", columnList = "brand_id,status,is_variation,created_at"),
    @Index(name = "idx_products_sale_type", columnList = "sale_type"),
    @Index(name = "idx_products_start_date", columnList = "start_date"),
    @Index(name = "idx_products_end_date", columnList = "end_date"),
    @Index(name = "idx_products_sale_price", columnList = "sale_price"),
    @Index(name = "idx_products_is_variation", columnList = "is_variation"),
    @Index(name = "idx_products_sku", columnList = "sku"),
    @Index(name = "idx_products_store_variation_status", columnList = "store_id,is_variation,status"),
    @Index(name = "idx_products_variation_name_id", columnList = "is_variation,name,id"),
    @Index(name = "idx_products_status", columnList = "status"),
    @Index(name = "idx_products_is_featured", columnList = "is_featured")
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
    
    @Column(nullable = false, length = 191)
    private String name;
    
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
    
    @Column(length = 191, unique = true)
    private String sku;
    
    @Column(name = "order", nullable = false)
    @Builder.Default
    private Integer order = 0;
    
    private Integer quantity;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowCheckoutWhenOutOfStock = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean withStorehouseManagement = false;
    
    @Column(length = 191)
    @Builder.Default
    private String stockStatus = "in_stock";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;
    
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
    
    @Column(nullable = false)
    @Builder.Default
    private Long views = 0L;
    
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
    
    @Column(length = 60)
    @Builder.Default
    private String productType = "physical";
    
    @Column(length = 150)
    private String barcode;
    
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
        name = "ec_product_category_product",
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
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProductReview> reviews = new ArrayList<>();
    
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
