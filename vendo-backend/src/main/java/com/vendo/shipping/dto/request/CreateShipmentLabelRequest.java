package com.vendo.shipping.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentLabelRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Shipping method ID is required")
    private Long shippingMethodId;
    
    private String carrier; // FEDEX, UPS, DHL
    private String serviceType; // STANDARD, EXPRESS, OVERNIGHT
}

