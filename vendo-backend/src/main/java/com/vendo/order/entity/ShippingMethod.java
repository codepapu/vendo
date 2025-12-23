package com.vendo.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_methods", indexes = {
    @Index(name = "idx_shipping_methods_code", columnList = "code"),
    @Index(name = "idx_shipping_methods_is_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_shipping_methods_code", columnNames = {"code"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShippingMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;
    
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank
    @Size(max = 50)
    private String code;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    @Size(max = 100)
    private String carrier;
    
    @Column(name = "base_cost", nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal baseCost;
    
    @Column(name = "cost_per_kg", precision = 10, scale = 2)
    private BigDecimal costPerKg;
    
    @Column(name = "estimated_days")
    private Integer estimatedDays;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "shippingMethod", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<com.vendo.order.entity.Shipment> shipments = new java.util.ArrayList<>();
}
