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

@Entity
@Table(name = "ec_wish_lists", indexes = {
    @Index(name = "idx_wishlists_product_customer", columnList = "product_id,customer_id"),
    @Index(name = "idx_wishlists_customer_id", columnList = "customer_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@IdClass(Wishlist.WishlistId.class)
public class Wishlist {
    
    @Id
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Id
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, insertable = false, updatable = false)
    private Product product;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WishlistId implements java.io.Serializable {
        private Long customerId;
        private Long productId;
    }
}
