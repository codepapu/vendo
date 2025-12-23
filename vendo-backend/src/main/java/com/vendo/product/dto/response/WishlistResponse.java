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
public class WishlistResponse {
    
    private Long customerId;
    private Long productId;
    private ProductResponse product;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
