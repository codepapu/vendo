package com.vendo.marketing.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.marketing.dto.response.EmailMarketingResponse;
import com.vendo.marketing.service.EmailMarketingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/marketing/email")
@RequiredArgsConstructor
public class EmailMarketingController {
    
    private final EmailMarketingService emailMarketingService;
    
    @PostMapping("/sync-segments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailMarketingResponse>> syncCustomerSegments(
            @RequestParam String segmentName,
            @RequestBody List<Long> userIds) {
        EmailMarketingResponse response = emailMarketingService.syncCustomerSegments(segmentName, userIds);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/campaigns/{campaignId}/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailMarketingResponse.CampaignMetrics>> trackCampaignPerformance(
            @PathVariable String campaignId) {
        EmailMarketingResponse.CampaignMetrics metrics = 
                emailMarketingService.trackCampaignPerformance(campaignId);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
    
    @PostMapping("/campaigns/automated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailMarketingResponse>> createAutomatedCampaign(
            @RequestParam String campaignName,
            @RequestParam String trigger,
            @RequestBody Map<String, Object> config) {
        EmailMarketingResponse response = emailMarketingService.createAutomatedCampaign(
                campaignName, trigger, config);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

