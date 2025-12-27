package com.vendo.b2b.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAccountResponse {
    
    private Long id;
    private Long userId;
    private String companyName;
    private String businessRegistrationNumber;
    private String taxId;
    private String businessType;
    private BigDecimal creditLimit;
    private Integer creditTermsDays;
    private String paymentTerms;
    private Long billingAddressId;
    private String status;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

