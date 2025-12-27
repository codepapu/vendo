package com.vendo.b2b.entity;

import com.vendo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "quote_revisions", indexes = {
    @Index(name = "idx_quote_revisions_quote_id", columnList = "quote_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_quote_revisions_quote_revision", columnNames = {"quote_id", "revision_number"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class QuoteRevision {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    @NotNull
    private Quote quote;
    
    @Column(name = "revision_number", nullable = false)
    @NotNull
    private Integer revisionNumber;
    
    @Column(length = 50)
    private String status;
    
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal totalAmount;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

