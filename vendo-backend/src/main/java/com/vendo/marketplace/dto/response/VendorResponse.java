package com.vendo.marketplace.dto.response;

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
public class VendorResponse {
    
    private Long id;
    private String storeName;
    private String storeSlug;
    private String description;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private Long userId;
    private String status;
    private BigDecimal totalSales;
    private Long totalOrders;
    private Long totalProducts;
    private BigDecimal commissionRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
