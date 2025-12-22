package com.vendo.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ec_order_returns", indexes = {
    @Index(name = "idx_order_returns_code", columnList = "code"),
    @Index(name = "idx_order_returns_order_id", columnList = "order_id"),
    @Index(name = "idx_order_returns_user_id", columnList = "user_id"),
    @Index(name = "idx_order_returns_return_status", columnList = "return_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderReturn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 191, unique = true)
    private String code;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    private Long storeId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(length = 191)
    private String orderStatus;
    
    @Column(nullable = false, length = 191)
    private String returnStatus;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "orderReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderReturnItem> returnItems = new ArrayList<>();
    
    @OneToMany(mappedBy = "orderReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderReturnHistory> histories = new ArrayList<>();
}
