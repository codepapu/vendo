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

import com.vendo.product.entity.ReviewReply;

@Entity
@Table(name = "ec_reviews", indexes = {
    @Index(name = "idx_reviews_product_customer_status_created", columnList = "product_id,customer_id,status,created_at"),
    @Index(name = "idx_reviews_product_id", columnList = "product_id"),
    @Index(name = "idx_reviews_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_reviews_product_customer", columnNames = {"product_id", "customer_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(length = 191)
    private String customerName;
    
    @Column(length = 191)
    private String customerEmail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal star;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "published";
    
    @Column(columnDefinition = "TEXT")
    private String images;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewReply> replies = new ArrayList<>();
}
