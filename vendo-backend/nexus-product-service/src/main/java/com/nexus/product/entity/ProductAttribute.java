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

@Entity
@Table(name = "ec_product_attributes", indexes = {
    @Index(name = "idx_product_attributes_attribute_set_id", columnList = "attribute_set_id"),
    @Index(name = "idx_product_attributes_set_order_id", columnList = "attribute_set_id,order,id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductAttribute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_set_id", nullable = false)
    private ProductAttributeSet attributeSet;
    
    @Column(nullable = false, length = 120)
    private String title;
    
    @Column(length = 120)
    private String slug;
    
    @Column(length = 120)
    private String color;
    
    @Column(length = 191)
    private String image;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(name = "order", nullable = false)
    @Builder.Default
    private Integer order = 0;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
