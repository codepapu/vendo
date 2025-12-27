package com.vendo.inventory.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.inventory.dto.request.AutoReorderSettingsRequest;
import com.vendo.inventory.dto.response.AutoReorderSettingsResponse;
import com.vendo.inventory.service.AutoReorderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory/auto-reorder")
@RequiredArgsConstructor
public class AutoReorderController {
    
    private final AutoReorderService autoReorderService;
    
    @PostMapping("/settings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<AutoReorderSettingsResponse>> setupAutoReorder(
            @Valid @RequestBody AutoReorderSettingsRequest request) {
        AutoReorderSettingsResponse response = autoReorderService.setupAutoReorder(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/settings/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<AutoReorderSettingsResponse>> updateReorderSettings(
            @PathVariable Long id,
            @Valid @RequestBody AutoReorderSettingsRequest request) {
        AutoReorderSettingsResponse response = autoReorderService.updateReorderSettings(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> processAutoReorder(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId) {
        autoReorderService.processAutoReorder(retailerId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success("Auto reorder processed"));
    }
    
    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<AutoReorderSettingsResponse>>> getAllReorderSettings() {
        List<AutoReorderSettingsResponse> settings = autoReorderService.getAllReorderSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
    
    @GetMapping("/settings/product/{productId}/retailer/{retailerId}/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<AutoReorderSettingsResponse>> getReorderSettings(
            @PathVariable Long productId,
            @PathVariable(required = false) Long retailerId,
            @PathVariable(required = false) Long warehouseId) {
        AutoReorderSettingsResponse settings = autoReorderService.getReorderSettings(
                productId, retailerId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}

