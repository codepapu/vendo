package com.vendo.cart.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressRequest shippingAddress;
    
    @NotNull(message = "Billing address is required")
    @Valid
    private AddressRequest billingAddress;
    
    private String shippingMethod;
    
    private String couponCode;
    
    private String paymentMethod;
    
    private String notes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        @NotNull(message = "Name is required")
        private String name;
        
        @NotNull(message = "Phone is required")
        private String phone;
        
        @NotNull(message = "Email is required")
        private String email;
        
        @NotNull(message = "Address is required")
        private String address;
        
        private String city;
        
        private String state;
        
        private String zipCode;
        
        private String country;
    }
}
