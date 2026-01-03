package com.vendo.common.controller;

import com.vendo.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * API Information Controller
 * Provides basic API information and available endpoints
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiInfoController {
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Vendo API");
        info.put("version", "1.0.0");
        info.put("description", "Vendo E-commerce Platform API");
        info.put("documentation", "/swagger-ui.html");
        info.put("apiDocs", "/v3/api-docs");
        info.put("health", "/actuator/health");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("auth", "/api/v1/auth");
        endpoints.put("products", "/api/v1/products");
        endpoints.put("categories", "/api/v1/categories");
        endpoints.put("cart", "/api/v1/cart");
        endpoints.put("orders", "/api/v1/orders");
        endpoints.put("payments", "/api/v1/payments");
        endpoints.put("users", "/api/v1/users");
        endpoints.put("b2b", "/api/v1/b2b");
        endpoints.put("retailers", "/api/v1/retailers");
        info.put("endpoints", endpoints);
        
        return ResponseEntity.ok(ApiResponse.success("Welcome to Vendo API", info));
    }
}

