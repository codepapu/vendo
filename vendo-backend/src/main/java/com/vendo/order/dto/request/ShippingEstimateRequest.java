package com.vendo.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingEstimateRequest {
    
    @NotNull(message = "Address is required")
    private AddressInfo address;
    
    @NotNull(message = "Total weight is required")
    @Positive(message = "Weight must be positive")
    private BigDecimal totalWeight;
    
    // Optional: For multiple shipping addresses per order
    private List<AddressWeightInfo> addressWeights;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        @NotNull(message = "City is required")
        private String city;
        
        @NotNull(message = "State is required")
        private String state;
        
        @NotNull(message = "Postal code is required")
        private String postalCode;
        
        @NotNull(message = "Country is required")
        private String country;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressWeightInfo {
        @NotNull(message = "Address is required")
        private AddressInfo address;
        
        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be positive")
        private BigDecimal weight;
    }
}

