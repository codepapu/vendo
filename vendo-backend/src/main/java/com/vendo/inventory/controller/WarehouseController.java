package com.vendo.inventory.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.inventory.dto.request.WarehouseRequest;
import com.vendo.inventory.dto.response.WarehouseResponse;
import com.vendo.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    
    private final WarehouseService warehouseService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<WarehouseResponse>>> getAllWarehouses(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<WarehouseResponse> warehouses = warehouseService.getAllWarehouses(isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(warehouses));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllActiveWarehouses() {
        List<WarehouseResponse> warehouses = warehouseService.getAllActiveWarehouses();
        return ResponseEntity.ok(ApiResponse.success(warehouses));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable Long id) {
        WarehouseResponse warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(ApiResponse.success(warehouse));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseByCode(@PathVariable String code) {
        WarehouseResponse warehouse = warehouseService.getWarehouseByCode(code);
        return ResponseEntity.ok(ApiResponse.success(warehouse));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse warehouse = warehouseService.createWarehouse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Warehouse created successfully", warehouse));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse warehouse = warehouseService.updateWarehouse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Warehouse updated successfully", warehouse));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deleted successfully"));
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<WarehouseResponse>>> searchWarehouses(
            @RequestParam String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<WarehouseResponse> warehouses = warehouseService.searchWarehouses(keyword, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(warehouses));
    }
}

