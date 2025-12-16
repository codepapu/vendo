package com.nexus.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ec_customers", indexes = {
    @Index(name = "idx_customers_email", columnList = "email"),
    @Index(name = "idx_customers_status", columnList = "status"),
    @Index(name = "idx_customers_is_vendor", columnList = "is_vendor")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String name;
    
    @Column(length = 191, unique = true)
    private String email;
    
    @Column(nullable = false, length = 191)
    private String password;
    
    @Column(length = 191)
    private String avatar;
    
    private LocalDate dob;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 100)
    private String rememberToken;
    
    private LocalDateTime confirmedAt;
    
    @Column(length = 120)
    private String emailVerifyToken;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVendor = false;
    
    private LocalDateTime vendorVerifiedAt;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "activated";
    
    @Column(length = 400)
    private String blockReason;
    
    @Column(columnDefinition = "TEXT")
    private String privateNotes;
    
    @Column(length = 191)
    private String stripeAccountId;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean stripeAccountActive = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CustomerAddress> addresses = new ArrayList<>();
}
