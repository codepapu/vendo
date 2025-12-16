package com.nexus.order.entity;

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
@Table(name = "ec_order_addresses", indexes = {
    @Index(name = "idx_order_addresses_order_id", columnList = "order_id"),
    @Index(name = "idx_order_addresses_type", columnList = "order_id,type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String name;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 191)
    private String email;
    
    @Column(length = 120)
    private String country;
    
    @Column(length = 120)
    private String state;
    
    @Column(length = 120)
    private String city;
    
    @Column(length = 191)
    private String address;
    
    @Column(length = 20)
    private String zipCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String type = "shipping_address";
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
