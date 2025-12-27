package com.vendo.search.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    
    private List<ProductResult> products;
    private Long totalResults;
    private Integer page;
    private Integer size;
    private Map<String, List<Facet>> facets;
    private List<String> suggestions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductResult {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String imageUrl;
        private Double price;
        private Double salePrice;
        private Double rating;
        private Integer reviewCount;
        private String category;
        private String brand;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Facet {
        private String value;
        private Long count;
    }
}

