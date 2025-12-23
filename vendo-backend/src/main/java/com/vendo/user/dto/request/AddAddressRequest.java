package com.vendo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressRequest {
    
    @Size(max = 50, message = "Address type must be less than 50 characters")
    private String addressType; // SHIPPING, BILLING, BOTH
    
    @Size(max = 255, message = "Contact name must be less than 255 characters")
    private String contactName;
    
    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    private String addressLine1;
    
    @Size(max = 255, message = "Address line 2 must be less than 255 characters")
    private String addressLine2;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must be less than 100 characters")
    private String state;
    
    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must be less than 20 characters")
    private String postalCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must be less than 100 characters")
    private String country;
    
    private Boolean isDefault;
}
