package com.vendo.tax.entity;

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

/**
 * Tax Exemption Entity
 * Defines tax exemptions for B2B customers
 */
@Entity
@Table(name = "tax_exemptions", indexes = {
    @Index(name = "idx_tax_exemptions_business_account", columnList = "business_account_id"),
    @Index(name = "idx_tax_exemptions_user", columnList = "user_id"),
    @Index(name = "idx_tax_exemptions_active", columnList = "is_active"),
    @Index(name = "idx_tax_exemptions_type", columnList = "exemption_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TaxExemption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "business_account_id")
    private Long businessAccountId;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "exemption_type", nullable = false, length = 50)
    private String exemptionType; // FULL, PARTIAL, PRODUCT_TYPE, LOCATION
    
    @Column(length = 100)
    private String country;
    
    @Column(length = 100)
    private String state;
    
    @Column(name = "product_type", length = 50)
    private String productType;
    
    @Column(name = "exemption_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal exemptionPercentage = BigDecimal.valueOf(100.00); // 100% = full exemption
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until")
    private LocalDateTime validUntil;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

