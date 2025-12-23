package com.vendo.inventory.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.inventory.dto.request.StockMovementRequest;
import com.vendo.inventory.dto.response.StockMovementResponse;
import com.vendo.inventory.entity.Inventory;
import com.vendo.inventory.entity.StockMovement;
import com.vendo.inventory.repository.InventoryRepository;
import com.vendo.inventory.repository.StockMovementRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockMovementService {
    
    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getAllStockMovements(Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findAll(pageable);
        return movements.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public StockMovementResponse getStockMovementById(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement", "id", id));
        return mapToResponse(movement);
    }
    
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByInventoryId(Long inventoryId) {
        List<StockMovement> movements = stockMovementRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId);
        return movements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getStockMovementsByInventoryId(Long inventoryId, Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findByInventoryId(inventoryId, pageable);
        return movements.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByMovementType(String movementType) {
        List<StockMovement> movements = stockMovementRepository.findByMovementType(movementType);
        return movements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByReference(String referenceType, Long referenceId) {
        List<StockMovement> movements = stockMovementRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
        return movements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getStockMovementsByProduct(Long productId, Long retailerId, Long warehouseId, Pageable pageable) {
        Page<StockMovement> movements = stockMovementRepository.findByProductAndOptionalRetailerAndWarehouse(
                productId, retailerId, warehouseId, pageable);
        return movements.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovementsByDateRange(String movementType, LocalDateTime startDate, LocalDateTime endDate) {
        List<StockMovement> movements = stockMovementRepository.findByMovementTypeAndDateRange(movementType, startDate, endDate);
        return movements.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public StockMovementResponse createStockMovement(StockMovementRequest request, Long userId) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", request.getInventoryId()));
        
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        }
        
        StockMovement stockMovement = StockMovement.builder()
                .inventory(inventory)
                .movementType(request.getMovementType())
                .quantity(request.getQuantity())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .notes(request.getNotes())
                .createdBy(user)
                .build();
        
        StockMovement savedMovement = stockMovementRepository.save(stockMovement);
        log.info("Stock movement created with ID: {}", savedMovement.getId());
        
        return mapToResponse(savedMovement);
    }
    
    private StockMovementResponse mapToResponse(StockMovement movement) {
        StockMovementResponse.StockMovementResponseBuilder builder = StockMovementResponse.builder()
                .id(movement.getId())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .referenceType(movement.getReferenceType())
                .referenceId(movement.getReferenceId())
                .notes(movement.getNotes())
                .createdAt(movement.getCreatedAt());
        
        if (movement.getInventory() != null) {
            builder.inventoryId(movement.getInventory().getId());
            
            Inventory inventory = movement.getInventory();
            StockMovementResponse.InventoryInfo inventoryInfo = StockMovementResponse.InventoryInfo.builder()
                    .id(inventory.getId())
                    .productId(inventory.getProduct() != null ? inventory.getProduct().getId() : null)
                    .productName(inventory.getProduct() != null ? inventory.getProduct().getName() : null)
                    .warehouseId(inventory.getWarehouse() != null ? inventory.getWarehouse().getId() : null)
                    .warehouseName(inventory.getWarehouse() != null ? inventory.getWarehouse().getName() : null)
                    .build();
            builder.inventory(inventoryInfo);
        }
        
        if (movement.getCreatedBy() != null) {
            builder.createdBy(movement.getCreatedBy().getId());
            
            User user = movement.getCreatedBy();
            StockMovementResponse.UserInfo userInfo = StockMovementResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build();
            builder.createdByUser(userInfo);
        }
        
        return builder.build();
    }
}

