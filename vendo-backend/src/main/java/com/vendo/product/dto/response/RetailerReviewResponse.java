package com.vendo.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetailerReviewResponse {
    
    private Long id;
    private Long retailerId;
    private RetailerInfo retailer;
    private Long userId;
    private UserInfo user;
    private Long orderId;
    private Integer rating;
    private String reviewText;
    private Boolean isApproved;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetailerInfo {
        private Long id;
        private String storeName;
        private String storeSlug;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
    }
}

