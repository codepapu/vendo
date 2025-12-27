package com.vendo.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectShippingMethodRequest {
    
    @NotNull(message = "Shipping method ID is required")
    private Long shippingMethodId;
    
    // Optional: For multiple shipping addresses
    private List<AddressShippingMethod> addressShippingMethods;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressShippingMethod {
        @NotNull(message = "Address ID is required")
        private Long addressId;
        
        @NotNull(message = "Shipping method ID is required")
        private Long shippingMethodId;
    }
}

