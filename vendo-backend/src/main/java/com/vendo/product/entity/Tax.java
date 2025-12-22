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
@Table(name = "ec_taxes", indexes = {
    @Index(name = "idx_taxes_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tax {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String title;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "published";
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToMany
    @JoinTable(
        name = "ec_tax_products",
        joinColumns = @JoinColumn(name = "tax_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
