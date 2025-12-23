package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailerBankAccountResponse {
    
    private Long id;
    private Long retailerId;
    private String accountHolderName;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
    private String swiftCode;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}

