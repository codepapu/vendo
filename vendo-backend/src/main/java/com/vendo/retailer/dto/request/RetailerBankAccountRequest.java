package com.vendo.retailer.dto.request;

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
public class RetailerBankAccountRequest {
    
    @NotBlank(message = "Account holder name is required")
    @Size(max = 255, message = "Account holder name must not exceed 255 characters")
    private String accountHolderName;
    
    @NotBlank(message = "Account number is required")
    @Size(max = 100, message = "Account number must not exceed 100 characters")
    private String accountNumber;
    
    @NotBlank(message = "Bank name is required")
    @Size(max = 255, message = "Bank name must not exceed 255 characters")
    private String bankName;
    
    @Size(max = 50, message = "IFSC code must not exceed 50 characters")
    private String ifscCode;
    
    @Size(max = 50, message = "SWIFT code must not exceed 50 characters")
    private String swiftCode;
    
    @Builder.Default
    private Boolean isPrimary = false;
}

