package com.vendo.product.dto.response;

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
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private String content;
    private String status;
    private String images;
    private String videoMedia;
    private String sku;
    private Integer order;
    private Integer quantity;
    private Boolean allowCheckoutWhenOutOfStock;
    private Boolean withStorehouseManagement;
    private String stockStatus;
    private Boolean isFeatured;
    private Long brandId;
    private String brandName;
    private Boolean isVariation;
    private Integer saleType;
    private BigDecimal price;
    private BigDecimal salePrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;
    private Long taxId;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private List<Long> tagIds;
    private List<String> tagNames;
    private List<Long> collectionIds;
    private List<String> collectionNames;
    private Double averageRating;
    private Long reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
