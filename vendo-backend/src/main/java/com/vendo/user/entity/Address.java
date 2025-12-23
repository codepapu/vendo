package com.vendo.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_addresses_user_id", columnList = "user_id"),
    @Index(name = "idx_addresses_business_account_id", columnList = "business_account_id"),
    @Index(name = "idx_addresses_is_default", columnList = "is_default")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Optional relationship - BusinessAccount entity will be created in B2B phase
    @Column(name = "business_account_id")
    private Long businessAccountId;
    
    @Column(name = "address_type", length = 50)
    private String addressType; // SHIPPING, BILLING, BOTH
    
    @Column(name = "contact_name", length = 255)
    private String contactName;
    
    @Column(length = 20)
    @Size(max = 20)
    private String phone;
    
    @Column(name = "address_line1", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String addressLine1;
    
    @Column(name = "address_line2", length = 255)
    @Size(max = 255)
    private String addressLine2;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String city;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String state;
    
    @Column(name = "postal_code", nullable = false, length = 20)
    @NotBlank
    @Size(max = 20)
    private String postalCode;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String country;
    
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
