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

import java.time.LocalDateTime;

@Entity
@Table(name = "price_list_assignments", indexes = {
    @Index(name = "idx_price_list_assignments_price_list_id", columnList = "price_list_id"),
    @Index(name = "idx_price_list_assignments_business_account_id", columnList = "business_account_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_price_list_assignments_unique", columnNames = {"price_list_id", "business_account_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PriceListAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    @NotNull
    private PriceList priceList;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", nullable = false)
    @NotNull
    private BusinessAccount businessAccount;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "assigned_at", nullable = false)
    @CreatedDate
    private LocalDateTime assignedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
}

