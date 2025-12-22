package com.vendo.product.dto.request;

import jakarta.validation.constraints.*;
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
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 191, message = "Product name must not exceed 191 characters")
    private String name;
    
    private String description;
    
    private String content;
    
    @Pattern(regexp = "^(published|draft|pending)$", message = "Status must be published, draft, or pending")
    private String status = "published";
    
    private String images; // JSON array of image URLs
    
    private String videoMedia;
    
    @Size(max = 191, message = "SKU must not exceed 191 characters")
    private String sku;
    
    @Min(value = 0, message = "Order must be non-negative")
    private Integer order = 0;
    
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
    
    private Boolean allowCheckoutWhenOutOfStock = false;
    
    private Boolean withStorehouseManagement = false;
    
    @Pattern(regexp = "^(in_stock|out_of_stock|on_backorder)$", message = "Stock status must be in_stock, out_of_stock, or on_backorder")
    private String stockStatus = "in_stock";
    
    private Boolean isFeatured = false;
    
    private Long brandId;
    
    private Boolean isVariation = false;
    
    @Min(value = 0, message = "Sale type must be non-negative")
    @Max(value = 2, message = "Sale type must be 0, 1, or 2")
    private Integer saleType = 0;
    
    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;
    
    @DecimalMin(value = "0.0", message = "Sale price must be non-negative")
    private BigDecimal salePrice;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @DecimalMin(value = "0.0", message = "Length must be non-negative")
    private BigDecimal length;
    
    @DecimalMin(value = "0.0", message = "Width must be non-negative")
    private BigDecimal width;
    
    @DecimalMin(value = "0.0", message = "Height must be non-negative")
    private BigDecimal height;
    
    @DecimalMin(value = "0.0", message = "Weight must be non-negative")
    private BigDecimal weight;
    
    private Long taxId;
    
    private List<Long> categoryIds;
    
    private List<Long> tagIds;
    
    private List<Long> collectionIds;
}
