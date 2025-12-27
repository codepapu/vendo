package com.vendo.b2b.entity;

import com.vendo.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "bulk_orders", indexes = {
    @Index(name = "idx_bulk_orders_business_account_id", columnList = "business_account_id"),
    @Index(name = "idx_bulk_orders_status", columnList = "status"),
    @Index(name = "idx_bulk_orders_created_by", columnList = "created_by")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BulkOrder {
    
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
    
    @Column(name = "source_file_name", length = 255)
    private String sourceFileName; // original CSV/Excel filename
    
    @Column(name = "source_file_type", length = 50)
    private String sourceFileType; // CSV, XLSX, XLS
    
    @Column(name = "total_items", nullable = false)
    @Builder.Default
    private Integer totalItems = 0;
    
    @Column(name = "processed_items", nullable = false)
    @Builder.Default
    private Integer processedItems = 0;
    
    @Column(name = "failed_items", nullable = false)
    @Builder.Default
    private Integer failedItems = 0;
    
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING"; // PENDING, VALIDATING, PROCESSING, COMPLETED, FAILED, PARTIAL
    
    @Column(name = "validation_errors", columnDefinition = "JSONB")
    private String validationErrors; // store validation errors as JSON
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToMany(mappedBy = "bulkOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BulkOrderItem> bulkOrderItems = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

