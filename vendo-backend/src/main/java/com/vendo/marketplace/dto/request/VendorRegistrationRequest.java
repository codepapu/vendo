package com.vendo.marketplace.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegistrationRequest {
    
    @NotBlank(message = "Store name is required")
    private String storeName;
    
    @NotBlank(message = "Store slug is required")
    private String storeSlug;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String city;
    
    private String state;
    
    private String zipCode;
    
    private String country;
    
    @NotNull(message = "User ID is required")
    private Long userId;
}
