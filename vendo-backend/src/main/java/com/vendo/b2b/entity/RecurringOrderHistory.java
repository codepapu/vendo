package com.vendo.b2b.entity;

import com.vendo.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_order_history", indexes = {
    @Index(name = "idx_recurring_order_history_recurring_order_id", columnList = "recurring_order_id"),
    @Index(name = "idx_recurring_order_history_order_id", columnList = "order_id"),
    @Index(name = "idx_recurring_order_history_scheduled_date", columnList = "scheduled_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RecurringOrderHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_order_id", nullable = false)
    @NotNull
    private RecurringOrder recurringOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(name = "scheduled_date", nullable = false)
    @NotNull
    private LocalDate scheduledDate;
    
    @Column(name = "generated_at", nullable = false)
    @CreatedDate
    private LocalDateTime generatedAt;
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "GENERATED"; // GENERATED, FAILED, SKIPPED
}

