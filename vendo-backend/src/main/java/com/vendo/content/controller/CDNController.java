package com.vendo.content.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.content.dto.request.InvalidateCacheRequest;
import com.vendo.content.dto.response.CDNResponse;
import com.vendo.content.dto.response.CDNStatsResponse;
import com.vendo.content.service.CDNService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cdn")
@RequiredArgsConstructor
@Tag(name = "CDN Management", description = "CDN integration and cache management endpoints")
public class CDNController {
    
    private final CDNService cdnService;
    
    @GetMapping("/url")
    @Operation(summary = "Get CDN URL", description = "Get CDN URL for a given path")
    public ResponseEntity<ApiResponse<CDNResponse>> getCDNUrl(
            @RequestParam String originalUrl,
            @RequestParam String path
    ) {
        CDNResponse response = cdnService.getCDNUrl(originalUrl, path);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/invalidate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Invalidate CDN cache", description = "Invalidate CDN cache for specific paths or patterns")
    public ResponseEntity<ApiResponse<Void>> invalidateCache(
            @Valid @RequestBody InvalidateCacheRequest request
    ) {
        cdnService.invalidateCache(request);
        return ResponseEntity.ok(ApiResponse.success("CDN cache invalidated successfully"));
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get CDN statistics", description = "Get CDN performance and usage statistics")
    public ResponseEntity<ApiResponse<CDNStatsResponse>> getCDNStats() {
        CDNStatsResponse stats = cdnService.getCDNStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

