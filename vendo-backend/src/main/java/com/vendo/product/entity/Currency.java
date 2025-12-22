package com.vendo.product.entity;

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
@Table(name = "ec_currencies", indexes = {
    @Index(name = "idx_currencies_status", columnList = "status"),
    @Index(name = "idx_currencies_is_default", columnList = "is_default")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Currency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 191)
    private String title;
    
    @Column(nullable = false, length = 10)
    private String symbol;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrefixSymbol = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer decimals = 0;
    
    @Column(nullable = false, precision = 15, scale = 8)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer order = 0;
    
    @Column(nullable = false, length = 60)
    @Builder.Default
    private String status = "published";
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
