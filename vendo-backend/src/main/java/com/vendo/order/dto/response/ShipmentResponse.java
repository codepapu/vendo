package com.vendo.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    
    private Long id;
    private Long orderId;
    private Long shippingMethodId;
    private String trackingNumber;
    private String carrier;
    private String status; // PENDING, IN_TRANSIT, DELIVERED, FAILED
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

