package com.vendo.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoices_invoice_number", columnList = "invoice_number"),
    @Index(name = "idx_invoices_order_id", columnList = "order_id"),
    @Index(name = "idx_invoices_business_account_id", columnList = "business_account_id"),
    @Index(name = "idx_invoices_status", columnList = "status"),
    @Index(name = "idx_invoices_issue_date", columnList = "issue_date"),
    @Index(name = "idx_invoices_due_date", columnList = "due_date"),
    @Index(name = "idx_invoices_created_at", columnList = "created_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_invoices_invoice_number", columnNames = {"invoice_number"})
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
    
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 50)
    private String invoiceNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id")
    private com.vendo.b2b.entity.BusinessAccount businessAccount;
    
    @Column(name = "issue_date", nullable = false)
    @jakarta.validation.constraints.NotNull
    private LocalDate issueDate;
    
    @Column(name = "due_date", nullable = false)
    @jakarta.validation.constraints.NotNull
    private LocalDate dueDate;
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, OVERDUE, CANCELLED
    
    @Column(nullable = false, precision = 15, scale = 2)
    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Subtotal must be non-negative")
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @jakarta.validation.constraints.NotNull
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Total amount must be non-negative")
    private BigDecimal totalAmount;
    
    @Column(name = "paid_amount", precision = 15, scale = 2)
    @Builder.Default
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Paid amount must be non-negative")
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceItem> invoiceItems = new ArrayList<>();
}
