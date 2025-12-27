package com.vendo.promotion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyResponse {
    
    private Long id;
    private Long userId;
    private BigDecimal pointsBalance;
    private String tier; // BRONZE, SILVER, GOLD, PLATINUM
    private BigDecimal redeemableValue;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LoyaltyTransaction> history;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoyaltyTransaction {
        private Long id;
        private String transactionType;
        private BigDecimal points;
        private String description;
        private LocalDateTime createdAt;
    }
}

