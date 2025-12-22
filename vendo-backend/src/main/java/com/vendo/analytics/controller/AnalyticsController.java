package com.vendo.analytics.controller;

import com.vendo.analytics.dto.response.AnalyticsDashboardResponse;
import com.vendo.analytics.service.AnalyticsService;
import com.vendo.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final AnalyticsService analyticsService;
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnalyticsDashboardResponse>> getDashboard() {
        AnalyticsDashboardResponse dashboard = analyticsService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
    
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<Void>> trackEvent(
            @RequestParam String eventType,
            @RequestParam(required = false) String sessionId,
            @RequestBody(required = false) Map<String, Object> properties,
            Authentication authentication) {
        Long userId = authentication != null ? Long.parseLong(authentication.getName()) : null;
        analyticsService.trackEvent(eventType, userId, sessionId, properties);
        return ResponseEntity.ok(ApiResponse.success("Event tracked successfully"));
    }
}
