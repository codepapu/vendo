package com.vendo.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMethodResponse {
    
    private Long id;
    private String name;
    private String code;
    private String description;
    private String carrier;
    private BigDecimal baseCost;
    private BigDecimal costPerKg;
    private Integer estimatedDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    
    // Calculated fields for shipping estimates
    private BigDecimal calculatedCost;
    private BigDecimal totalWeight;
}

