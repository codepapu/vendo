package com.vendo.search.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.search.dto.response.SearchResponse;
import com.vendo.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<SearchResponse>> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        SearchResponse response = searchService.searchProducts(
                query, category, minPrice, maxPrice, brand, minRating, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<String> suggestions = searchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}

