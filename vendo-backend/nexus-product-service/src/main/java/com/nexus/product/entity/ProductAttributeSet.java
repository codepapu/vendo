package com.nexus.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ec_product_attribute_sets", indexes = {
    @Index(name = "idx_attribute_sets_status", columnList = "status"),
    @Index(name = "idx_attribute_sets_slug", columnList = "slug")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductAttributeSet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String title;
    
    @Column(length = 191, unique = true)
    private String slug;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String displayLayout = "dropdown";
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSearchable = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isComparable = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isUseInProductListing = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean useImageFromProductVariation = false;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "published";
    
    @Column(nullable = false)
    @Builder.Default
    private Integer order = 0;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "attributeSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();
}
