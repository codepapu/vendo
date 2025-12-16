package com.nexus.user.entity;

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
@Table(name = "ec_customer_addresses", indexes = {
    @Index(name = "idx_customer_addresses_customer_id", columnList = "customer_id"),
    @Index(name = "idx_customer_addresses_is_default", columnList = "customer_id,is_default")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CustomerAddress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @Column(nullable = false, length = 60)
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
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
