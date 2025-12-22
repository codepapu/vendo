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
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_parent_id", columnList = "parent_id"),
    @Index(name = "idx_categories_status", columnList = "status"),
    @Index(name = "idx_categories_order", columnList = "order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> children = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "published";
    
    @Column(nullable = false)
    @Builder.Default
    private Integer order = 0;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;
    
    @Column(length = 191)
    private String image;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
