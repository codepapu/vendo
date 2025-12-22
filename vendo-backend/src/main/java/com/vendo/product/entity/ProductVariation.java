package com.vendo.product.entity;

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
@Table(name = "ec_product_variations", indexes = {
    @Index(name = "idx_product_variations_product_configurable", columnList = "product_id,configurable_product_id"),
    @Index(name = "idx_product_variations_configurable_product_id", columnList = "configurable_product_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_product_variations_product_configurable", columnNames = {"product_id", "configurable_product_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductVariation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // The variation product
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configurable_product_id", nullable = false)
    private Product configurableProduct; // The parent configurable product
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "variation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariationItem> variationItems = new ArrayList<>();
}
