package com.vendo.order.dto.response;

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
public class ShippingEstimateResponse {
    
    private List<ShippingMethodResponse> availableMethods;
    private BigDecimal totalWeight;
    private AddressInfo shippingAddress;
    
    // For multiple shipping addresses
    private List<AddressShippingEstimate> addressEstimates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressInfo {
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressShippingEstimate {
        private AddressInfo address;
        private BigDecimal weight;
        private List<ShippingMethodResponse> availableMethods;
    }
}

