package com.vendo.retailer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_import_history")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retailer_id", nullable = false)
    private Retailer retailer;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "file_type", length = 50)
    private String fileType; // CSV, XLSX, XLS
    
    @Column(name = "total_records")
    private Integer totalRecords;
    
    @Column(name = "success_count")
    private Integer successCount;
    
    @Column(name = "error_count")
    private Integer errorCount;
    
    @Column(name = "status", length = 50)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails; // JSON string of errors
    
    @Column(name = "created_by")
    private Long createdBy; // User ID
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

