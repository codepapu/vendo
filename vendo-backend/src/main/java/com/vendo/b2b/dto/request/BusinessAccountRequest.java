package com.vendo.b2b.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccountRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;
    
    @Size(max = 100, message = "Business registration number must not exceed 100 characters")
    private String businessRegistrationNumber;
    
    @Size(max = 100, message = "Tax ID must not exceed 100 characters")
    private String taxId;
    
    @Size(max = 50, message = "Business type must not exceed 50 characters")
    private String businessType; // CORPORATE, SME, ENTERPRISE
    
    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Credit limit must be non-negative")
    private BigDecimal creditLimit;
    
    @jakarta.validation.constraints.Min(value = 1, message = "Credit terms days must be positive")
    private Integer creditTermsDays;
    
    private String paymentTerms;
    
    private Long billingAddressId;
}

