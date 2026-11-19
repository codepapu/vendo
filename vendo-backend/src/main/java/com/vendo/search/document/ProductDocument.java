package com.vendo.search.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch document model for Product search
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {
    
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Keyword)
    private String slug;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String imageUrl;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    @Field(type = FieldType.Double)
    private Double salePrice;
    
    @Field(type = FieldType.Double)
    private Double rating;
    
    @Field(type = FieldType.Long)
    private Long reviewCount;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private List<String> categories;
    
    @Field(type = FieldType.Keyword)
    private String brand;
    
    @Field(type = FieldType.Long)
    private Long brandId;
    
    @Field(type = FieldType.Keyword)
    private String status;
    
    @Field(type = FieldType.Boolean)
    private Boolean isFeatured;
    
    @Field(type = FieldType.Keyword)
    private String sku;
    
    @Field(type = FieldType.Integer)
    private Integer quantity;
    
    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;
    
    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
    
    // Additional fields for faceted search
    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword)
        }
    )
    private List<String> tags;
}

