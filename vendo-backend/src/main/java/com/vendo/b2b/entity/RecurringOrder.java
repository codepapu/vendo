package com.vendo.b2b.entity;

import com.vendo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "recurring_orders", indexes = {
    @Index(name = "idx_recurring_orders_business_account_id", columnList = "business_account_id"),
    @Index(name = "idx_recurring_orders_status", columnList = "status"),
    @Index(name = "idx_recurring_orders_next_order_date", columnList = "next_order_date"),
    @Index(name = "idx_recurring_orders_created_by", columnList = "created_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RecurringOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", nullable = false)
    @NotNull
    private BusinessAccount businessAccount;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "recurrence_pattern", nullable = false, length = 50)
    @NotBlank
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY, QUARTERLY
    
    @Column(name = "recurrence_value", nullable = false)
    @NotNull
    @Positive
    @Builder.Default
    private Integer recurrenceValue = 1; // e.g., every 2 weeks, every 3 months
    
    @Column(name = "start_date", nullable = false)
    @NotNull
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "next_order_date", nullable = false)
    @NotNull
    private LocalDate nextOrderDate;
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, PAUSED, CANCELLED, COMPLETED
    
    @Column(name = "notification_days_before", nullable = false)
    @Builder.Default
    private Integer notificationDaysBefore = 3; // notify X days before next order
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToMany(mappedBy = "recurringOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecurringOrderItem> recurringOrderItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "recurringOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RecurringOrderHistory> recurringOrderHistory = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

