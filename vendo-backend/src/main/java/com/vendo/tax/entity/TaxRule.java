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
 * Tax Rule Entity
 * Defines tax rules based on location, product type, or business type
 */
@Entity
@Table(name = "tax_rules", indexes = {
    @Index(name = "idx_tax_rules_type", columnList = "rule_type"),
    @Index(name = "idx_tax_rules_country", columnList = "country"),
    @Index(name = "idx_tax_rules_state", columnList = "state"),
    @Index(name = "idx_tax_rules_product_type", columnList = "product_type"),
    @Index(name = "idx_tax_rules_business_type", columnList = "business_type"),
    @Index(name = "idx_tax_rules_active", columnList = "is_active"),
    @Index(name = "idx_tax_rules_priority", columnList = "priority")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TaxRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", nullable = false, length = 191)
    private String ruleName;
    
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType; // LOCATION, PRODUCT_TYPE, BUSINESS_TYPE, DEFAULT
    
    @Column(length = 100)
    private String country;
    
    @Column(length = 100)
    private String state;
    
    @Column(length = 100)
    private String city;
    
    @Column(name = "product_type", length = 50)
    private String productType; // SIMPLE, VARIABLE, GROUPED, DIGITAL, PHYSICAL
    
    @Column(name = "business_type", length = 50)
    private String businessType; // CORPORATE, SME, ENTERPRISE, B2C
    
    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "applies_to_b2b", nullable = false)
    @Builder.Default
    private Boolean appliesToB2b = true;
    
    @Column(name = "exemption_conditions", columnDefinition = "TEXT")
    private String exemptionConditions; // JSON string for complex exemption conditions
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

