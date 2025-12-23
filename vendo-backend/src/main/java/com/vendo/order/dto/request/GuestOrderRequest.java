package com.vendo.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for guest checkout (non-authenticated users)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderRequest {
    
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressRequest shippingAddress;
    
    @NotNull(message = "Billing address is required")
    @Valid
    private AddressRequest billingAddress;
    
    @NotNull(message = "Email is required for guest checkout")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotNull(message = "First name is required")
    private String firstName;
    
    private String lastName;
    
    private String phone;
    
    private String shippingMethod;
    
    private String couponCode;
    
    private String notes;
    
    // Optional: If guest wants to create account after checkout
    private Boolean createAccount;
    
    private String password; // Required if createAccount is true
}
