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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ec_shipping", indexes = {
    @Index(name = "idx_shipping_country", columnList = "country"),
    @Index(name = "idx_shipping_is_enabled", columnList = "is_enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shipping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String title;
    
    @Column(length = 120)
    private String country;
    
    @Column(length = 120)
    private String state;
    
    @Column(length = 120)
    private String city;
    
    @Column(length = 20)
    private String zipCode;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "shipping", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShippingRule> rules = new ArrayList<>();
}
