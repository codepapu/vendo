package com.vendo.integration.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.integration.dto.request.IntegrationConfigRequest;
import com.vendo.integration.dto.response.AccountingIntegrationResponse;
import com.vendo.integration.dto.response.CRMIntegrationResponse;
import com.vendo.integration.dto.response.ERPIntegrationResponse;
import com.vendo.integration.dto.response.IntegrationResponse;
import com.vendo.integration.entity.IntegrationConfiguration;
import com.vendo.integration.service.AccountingIntegrationService;
import com.vendo.integration.service.CRMIntegrationService;
import com.vendo.integration.service.ERPIntegrationService;
import com.vendo.integration.service.IntegrationConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationController {
    
    private final IntegrationConfigService configService;
    private final AccountingIntegrationService accountingService;
    private final CRMIntegrationService crmService;
    private final ERPIntegrationService erpService;
    
    // ==================== Configuration Management ====================
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<IntegrationResponse>>> getAllConfigurations(Pageable pageable) {
        Page<IntegrationResponse> configs = configService.getAllConfigurations(pageable);
        return ResponseEntity.ok(ApiResponse.success("Integration configurations retrieved", configs));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IntegrationResponse>> getConfigurationById(@PathVariable Long id) {
        IntegrationResponse config = configService.getConfigurationById(id);
        return ResponseEntity.ok(ApiResponse.success("Integration configuration retrieved", config));
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<IntegrationResponse>>> getConfigurationsByType(
            @PathVariable IntegrationConfiguration.IntegrationType type,
            Pageable pageable) {
        Page<IntegrationResponse> configs = configService.getConfigurationsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success("Integration configurations retrieved", configs));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<IntegrationResponse>> createConfiguration(
            @Valid @RequestBody IntegrationConfigRequest request,
            @AuthenticationPrincipal Long userId) {
        IntegrationResponse config = configService.createConfiguration(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Integration configuration created", config));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IntegrationResponse>> updateConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody IntegrationConfigRequest request) {
        IntegrationResponse config = configService.updateConfiguration(id, request);
        return ResponseEntity.ok(ApiResponse.success("Integration configuration updated", config));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfiguration(@PathVariable Long id) {
        configService.deleteConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success("Integration configuration deleted", null));
    }
    
    @PostMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<IntegrationResponse>> enableConfiguration(@PathVariable Long id) {
        IntegrationResponse config = configService.enableConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success("Integration configuration enabled", config));
    }
    
    @PostMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<IntegrationResponse>> disableConfiguration(@PathVariable Long id) {
        IntegrationResponse config = configService.disableConfiguration(id);
        return ResponseEntity.ok(ApiResponse.success("Integration configuration disabled", config));
    }
    
    // ==================== Accounting Integration ====================
    
    @PostMapping("/accounting/{id}/sync")
    public ResponseEntity<ApiResponse<AccountingIntegrationResponse>> syncToAccounting(@PathVariable Long id) {
        AccountingIntegrationResponse response = accountingService.syncToAccounting(id);
        return ResponseEntity.ok(ApiResponse.success("Accounting sync completed", response));
    }
    
    @PostMapping("/accounting/{id}/sync/inbound")
    public ResponseEntity<ApiResponse<AccountingIntegrationResponse>> syncFromAccounting(@PathVariable Long id) {
        AccountingIntegrationResponse response = accountingService.syncFromAccounting(id);
        return ResponseEntity.ok(ApiResponse.success("Accounting inbound sync completed", response));
    }
    
    @PostMapping("/accounting/{id}/webhook")
    public ResponseEntity<ApiResponse<AccountingIntegrationResponse>> handleAccountingWebhook(
            @PathVariable Long id,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        AccountingIntegrationResponse response = accountingService.handleWebhook(id, payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", response));
    }
    
    // ==================== CRM Integration ====================
    
    @PostMapping("/crm/{id}/sync")
    public ResponseEntity<ApiResponse<CRMIntegrationResponse>> syncToCRM(@PathVariable Long id) {
        CRMIntegrationResponse response = crmService.syncToCRM(id);
        return ResponseEntity.ok(ApiResponse.success("CRM sync completed", response));
    }
    
    @PostMapping("/crm/{id}/sync/inbound")
    public ResponseEntity<ApiResponse<CRMIntegrationResponse>> syncFromCRM(@PathVariable Long id) {
        CRMIntegrationResponse response = crmService.syncFromCRM(id);
        return ResponseEntity.ok(ApiResponse.success("CRM inbound sync completed", response));
    }
    
    @PostMapping("/crm/{id}/webhook")
    public ResponseEntity<ApiResponse<CRMIntegrationResponse>> handleCRMWebhook(
            @PathVariable Long id,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        CRMIntegrationResponse response = crmService.handleWebhook(id, payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", response));
    }
    
    // ==================== ERP Integration ====================
    
    @PostMapping("/erp/{id}/sync")
    public ResponseEntity<ApiResponse<ERPIntegrationResponse>> syncToERP(@PathVariable Long id) {
        ERPIntegrationResponse response = erpService.syncToERP(id);
        return ResponseEntity.ok(ApiResponse.success("ERP sync completed", response));
    }
    
    @PostMapping("/erp/{id}/sync/inbound")
    public ResponseEntity<ApiResponse<ERPIntegrationResponse>> syncFromERP(@PathVariable Long id) {
        ERPIntegrationResponse response = erpService.syncFromERP(id);
        return ResponseEntity.ok(ApiResponse.success("ERP inbound sync completed", response));
    }
    
    @PostMapping("/erp/{id}/webhook")
    public ResponseEntity<ApiResponse<ERPIntegrationResponse>> handleERPWebhook(
            @PathVariable Long id,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature) {
        ERPIntegrationResponse response = erpService.handleWebhook(id, payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", response));
    }
}

