package com.nexus.order.entity;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ec_invoices", indexes = {
    @Index(name = "idx_invoices_code", columnList = "code"),
    @Index(name = "idx_invoices_reference", columnList = "reference_type,reference_id"),
    @Index(name = "idx_invoices_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String referenceType;
    
    @Column(nullable = false)
    private Long referenceId;
    
    @Column(nullable = false, length = 191, unique = true)
    private String code;
    
    @Column(length = 191)
    private String customerName;
    
    @Column(length = 191)
    private String companyName;
    
    @Column(length = 191)
    private String companyLogo;
    
    @Column(length = 191)
    private String customerEmail;
    
    @Column(length = 191)
    private String customerPhone;
    
    @Column(length = 191)
    private String customerAddress;
    
    @Column(length = 191)
    private String customerCountry;
    
    @Column(length = 191)
    private String customerState;
    
    @Column(length = 191)
    private String customerCity;
    
    @Column(length = 191)
    private String customerZipCode;
    
    @Column(length = 191)
    private String customerAddressLine;
    
    @Column(length = 191)
    private String customerTaxId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subTotal;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paymentFee = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(length = 60)
    private String shippingOption;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String shippingMethod = "default";
    
    @Column(length = 120)
    private String couponCode;
    
    @Column(length = 191)
    private String discountDescription;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Long paymentId;
    
    @Column(nullable = false, length = 191)
    @Builder.Default
    private String status = "pending";
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceItem> invoiceItems = new ArrayList<>();
}
