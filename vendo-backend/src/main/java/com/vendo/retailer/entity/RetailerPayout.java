package com.vendo.retailer.entity;

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

@Entity
@Table(name = "retailer_payouts")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailerPayout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    private Retailer retailer;
    
    @Column(name = "payout_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal payoutAmount;
    
    @Column(name = "total_sales", precision = 15, scale = 2)
    private BigDecimal totalSales;
    
    @Column(name = "total_commission", precision = 15, scale = 2)
    private BigDecimal totalCommission;
    
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;
    
    @Column(name = "status", length = 50)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    
    @Column(name = "payout_method", length = 50)
    private String payoutMethod; // BANK_TRANSFER, PAYPAL, STRIPE
    
    @Column(name = "transaction_id", length = 255)
    private String transactionId;
    
    @Column(name = "bank_account_id")
    private Long bankAccountId; // Reference to RetailerBankAccount
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "processed_by")
    private Long processedBy; // User ID
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

