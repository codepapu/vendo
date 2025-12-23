package com.vendo.inventory.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.inventory.dto.request.InventoryRequest;
import com.vendo.inventory.dto.request.ReserveInventoryRequest;
import com.vendo.inventory.dto.request.ReleaseInventoryRequest;
import com.vendo.inventory.dto.request.UpdateInventoryRequest;
import com.vendo.inventory.dto.response.InventoryResponse;
import com.vendo.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getAllInventory(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<InventoryResponse> inventory = inventoryService.getAllInventory(productId, retailerId, warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable Long id) {
        InventoryResponse inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByProduct(@PathVariable Long productId) {
        List<InventoryResponse> inventory = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @GetMapping("/product/{productId}/retailer/{retailerId}/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductAndRetailerAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long retailerId,
            @PathVariable Long warehouseId) {
        InventoryResponse inventory = inventoryService.getInventoryByProductAndRetailerAndWarehouse(
                productId, retailerId, warehouseId);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getLowStockItems(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryResponse> inventory = inventoryService.getLowStockItems(retailerId, warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @GetMapping("/reorder-point")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getItemsAtReorderPoint(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryResponse> inventory = inventoryService.getItemsAtReorderPoint(retailerId, warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @GetMapping("/product/{productId}/available")
    public ResponseEntity<ApiResponse<Long>> getTotalAvailableQuantity(
            @PathVariable Long productId,
            @RequestParam(required = false) Long retailerId) {
        Long quantity = inventoryService.getTotalAvailableQuantity(productId, retailerId);
        return ResponseEntity.ok(ApiResponse.success(quantity));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody InventoryRequest request) {
        InventoryResponse inventory = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory created successfully", inventory));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        InventoryResponse inventory = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Inventory updated successfully", inventory));
    }
    
    @PostMapping("/reserve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveInventory(
            @Valid @RequestBody ReserveInventoryRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        InventoryResponse inventory = inventoryService.reserveInventory(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Inventory reserved successfully", inventory));
    }
    
    @PostMapping("/release")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseInventory(
            @Valid @RequestBody ReleaseInventoryRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        InventoryResponse inventory = inventoryService.releaseInventory(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Inventory released successfully", inventory));
    }
    
    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustInventory(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        InventoryResponse inventory = inventoryService.adjustInventory(id, quantity, notes, userId);
        return ResponseEntity.ok(ApiResponse.success("Inventory adjusted successfully", inventory));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory deleted successfully"));
    }
}

