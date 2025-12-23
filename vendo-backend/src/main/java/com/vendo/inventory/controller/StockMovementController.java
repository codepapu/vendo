package com.vendo.inventory.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.inventory.dto.request.StockMovementRequest;
import com.vendo.inventory.dto.response.StockMovementResponse;
import com.vendo.inventory.service.StockMovementService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {
    
    private final StockMovementService stockMovementService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getAllStockMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<StockMovementResponse> movements = stockMovementService.getAllStockMovements(pageable);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<StockMovementResponse>> getStockMovementById(@PathVariable Long id) {
        StockMovementResponse movement = stockMovementService.getStockMovementById(id);
        return ResponseEntity.ok(ApiResponse.success(movement));
    }
    
    @GetMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsByInventoryId(
            @PathVariable Long inventoryId) {
        List<StockMovementResponse> movements = stockMovementService.getStockMovementsByInventoryId(inventoryId);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @GetMapping("/inventory/{inventoryId}/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getStockMovementsByInventoryId(
            @PathVariable Long inventoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockMovementResponse> movements = stockMovementService.getStockMovementsByInventoryId(inventoryId, pageable);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @GetMapping("/type/{movementType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsByMovementType(
            @PathVariable String movementType) {
        List<StockMovementResponse> movements = stockMovementService.getStockMovementsByMovementType(movementType);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @GetMapping("/reference")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsByReference(
            @RequestParam String referenceType,
            @RequestParam Long referenceId) {
        List<StockMovementResponse> movements = stockMovementService.getStockMovementsByReference(referenceType, referenceId);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<StockMovementResponse>>> getStockMovementsByProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockMovementResponse> movements = stockMovementService.getStockMovementsByProduct(
                productId, retailerId, warehouseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsByDateRange(
            @RequestParam(required = false) String movementType,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<StockMovementResponse> movements = stockMovementService.getStockMovementsByDateRange(
                movementType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(movements));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<StockMovementResponse>> createStockMovement(
            @Valid @RequestBody StockMovementRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        StockMovementResponse movement = stockMovementService.createStockMovement(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Stock movement created successfully", movement));
    }
}

