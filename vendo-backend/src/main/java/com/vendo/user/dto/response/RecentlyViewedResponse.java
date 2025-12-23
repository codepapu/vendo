package com.vendo.user.dto.response;

import com.vendo.product.dto.response.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentlyViewedResponse {
    
    private Long id;
    private Long customerId;
    private Long productId;
    private ProductResponse product;
    private LocalDateTime viewedAt;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentlyViewedListResponse {
        private List<RecentlyViewedResponse> items;
        private Integer totalCount;
    }
}
