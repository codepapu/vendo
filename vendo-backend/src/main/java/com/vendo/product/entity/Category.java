package com.vendo.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    
    @Column(nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
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
    
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;
    
    // Alias for sortOrder
    public Integer getOrder() {
        return sortOrder;
    }
    
    public void setOrder(Integer order) {
        this.sortOrder = order;
    }
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;
    
    @Column(name = "image_url", length = 500)
    @Size(max = 500)
    private String imageUrl;
    
    // Alias for imageUrl
    public String getImage() {
        return imageUrl;
    }
    
    public void setImage(String image) {
        this.imageUrl = image;
    }
    
    @Column(unique = true, length = 255)
    @Size(max = 255)
    private String slug;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
