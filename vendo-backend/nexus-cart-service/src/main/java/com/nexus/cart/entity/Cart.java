package com.nexus.cart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Cart entity for persistent cart storage
 * Note: In production, cart is typically stored in Redis for better performance
 * This entity is for persistent cart storage if needed
 */
@Entity
@Table(name = "ec_cart", indexes = {
    @Index(name = "idx_cart_identifier", columnList = "identifier"),
    @Index(name = "idx_cart_instance", columnList = "instance")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@IdClass(Cart.CartId.class)
public class Cart {
    
    @Id
    @Column(name = "identifier", nullable = false, length = 60)
    private String identifier;
    
    @Id
    @Column(name = "instance", nullable = false, length = 60)
    private String instance;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
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
    public static class CartId implements java.io.Serializable {
        private String identifier;
        private String instance;
    }
}
