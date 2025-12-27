package com.vendo.cart.dto.response;

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
public class CartResponse {
    
    private Long userId;
    private String cartType; // B2C, B2B, MULTI_VENDOR
    private Long businessAccountId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shipping;
    private BigDecimal total;
    private LocalDateTime updatedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer quantity;
    private BigDecimal itemTotal;
    private Long variationId;
    private Long retailerId;
    private String retailerName;
}
