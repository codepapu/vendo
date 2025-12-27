package com.vendo.shipping.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingResponse {
    
    private BigDecimal cost;
    private LocalDateTime estimatedDelivery;
    private String carrier;
    private String serviceType;
    private String trackingNumber;
    private String labelUrl;
    private List<ShippingMethod> availableMethods;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingMethod {
        private Long id;
        private String name;
        private String code;
        private String carrier;
        private BigDecimal cost;
        private Integer estimatedDays;
        private LocalDateTime estimatedDelivery;
    }
}

