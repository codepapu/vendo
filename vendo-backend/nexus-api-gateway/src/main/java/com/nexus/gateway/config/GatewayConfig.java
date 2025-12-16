package com.nexus.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/auth/**")
                        .uri("lb://nexus-user-service"))
                
                // Product Service (Future)
                .route("product-service", r -> r
                        .path("/api/v1/products/**", "/api/v1/categories/**")
                        .uri("lb://nexus-product-service"))
                
                // Order Service (Future)
                .route("order-service", r -> r
                        .path("/api/v1/orders/**", "/api/v1/cart/**", "/api/v1/checkout/**")
                        .uri("lb://nexus-order-service"))
                
                .build();
    }
}
