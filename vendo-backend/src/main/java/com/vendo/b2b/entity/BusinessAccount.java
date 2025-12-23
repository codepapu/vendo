package com.vendo.b2b.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

@Entity
@Table(name = "business_accounts", indexes = {
    @Index(name = "idx_business_accounts_user_id", columnList = "user_id"),
    @Index(name = "idx_business_accounts_status", columnList = "status"),
    @Index(name = "idx_business_accounts_business_type", columnList = "business_type"),
    @Index(name = "idx_business_accounts_approved_by", columnList = "approved_by"),
    @Index(name = "idx_business_accounts_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BusinessAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.vendo.user.entity.User user;
    
    @Column(name = "company_name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String companyName;
    
    @Column(name = "business_registration_number", length = 100)
    @Size(max = 100)
    private String businessRegistrationNumber;
    
    @Column(name = "tax_id", length = 100)
    @Size(max = 100)
    private String taxId;
    
    @Column(name = "business_type", length = 50)
    @Size(max = 50)
    private String businessType; // CORPORATE, SME, ENTERPRISE
    
    @Column(name = "credit_limit", precision = 15, scale = 2)
    @Builder.Default
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Credit limit must be non-negative")
    private BigDecimal creditLimit = BigDecimal.ZERO;
    
    @Column(name = "credit_terms_days")
    @Builder.Default
    @jakarta.validation.constraints.Min(value = 1, message = "Credit terms days must be positive")
    private Integer creditTermsDays = 30;
    
    @Column(name = "payment_terms", columnDefinition = "TEXT")
    private String paymentTerms;
    
    @Column(name = "billing_address_id")
    private Long billingAddressId;
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, SUSPENDED
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private com.vendo.user.entity.User approvedBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "businessAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BusinessContact> contacts = new ArrayList<>();
    
    @OneToMany(mappedBy = "businessAccount", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.order.entity.PurchaseOrder> purchaseOrders = new ArrayList<>();
    
    @OneToMany(mappedBy = "businessAccount", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.order.entity.Invoice> invoices = new ArrayList<>();
    
    @OneToMany(mappedBy = "businessAccount", fetch = FetchType.LAZY)
    @Builder.Default
    private List<com.vendo.order.entity.Order> orders = new ArrayList<>();
}
