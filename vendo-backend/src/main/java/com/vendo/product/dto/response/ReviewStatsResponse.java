package com.vendo.product.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {
    
    private Long productId;
    private Double averageRating;
    private Long totalReviews;
    private Long verifiedReviews;
    private Map<Integer, Long> ratingDistribution; // Key: rating (1-5), Value: count
}
