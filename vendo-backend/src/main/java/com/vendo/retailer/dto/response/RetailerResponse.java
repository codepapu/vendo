package com.vendo.retailer.dto.response;

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
public class RetailerResponse {
    
    private Long id;
    private Long userId;
    private UserInfo user;
    private String storeName;
    private String storeSlug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private BigDecimal commissionRate;
    private String status;
    private BigDecimal rating;
    private BigDecimal totalSales;
    private Integer totalOrders;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
    }
}

