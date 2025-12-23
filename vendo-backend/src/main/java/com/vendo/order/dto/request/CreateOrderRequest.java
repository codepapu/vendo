package com.vendo.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private AddressRequest shippingAddress;
    
    @NotNull(message = "Billing address is required")
    @Valid
    private AddressRequest billingAddress;
    
    private Long businessAccountId; // For B2B orders
    
    private String orderType; // B2C, B2B, RETAILER (defaults to B2C if not provided)
    
    private String shippingMethod;
    
    private String couponCode;
    
    private String notes;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    
    private Long variationId;
    
    private Long retailerId; // For marketplace orders
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
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
