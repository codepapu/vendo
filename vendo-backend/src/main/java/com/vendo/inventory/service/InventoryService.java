package com.vendo.inventory.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.inventory.dto.request.InventoryRequest;
import com.vendo.inventory.dto.request.ReserveInventoryRequest;
import com.vendo.inventory.dto.request.ReleaseInventoryRequest;
import com.vendo.inventory.dto.request.UpdateInventoryRequest;
import com.vendo.inventory.dto.response.InventoryResponse;
import com.vendo.inventory.entity.Inventory;
import com.vendo.inventory.entity.StockMovement;
import com.vendo.inventory.repository.InventoryRepository;
import com.vendo.inventory.repository.StockMovementRepository;
import com.vendo.product.entity.Product;
import com.vendo.product.repository.ProductRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.inventory.entity.Warehouse;
import com.vendo.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMovementRepository stockMovementRepository;
    
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getAllInventory(Long productId, Long retailerId, Long warehouseId, Pageable pageable) {
        Page<Inventory> inventory;
        
        if (productId != null) {
            // If productId is provided, use the list method and convert to page
            List<Inventory> inventoryList = inventoryRepository.findByProductAndOptionalRetailerAndWarehouse(
                    productId, retailerId, warehouseId);
            // Filter by retailer and warehouse if provided
            if (retailerId != null) {
                inventoryList = inventoryList.stream()
                        .filter(inv -> inv.getRetailer() != null && inv.getRetailer().getId().equals(retailerId))
                        .collect(Collectors.toList());
            }
            if (warehouseId != null) {
                inventoryList = inventoryList.stream()
                        .filter(inv -> inv.getWarehouse() != null && inv.getWarehouse().getId().equals(warehouseId))
                        .collect(Collectors.toList());
            }
            // Convert list to page
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), inventoryList.size());
            List<Inventory> pagedList = start < inventoryList.size() ? inventoryList.subList(start, end) : List.of();
            inventory = new PageImpl<>(pagedList, pageable, inventoryList.size());
        } else if (retailerId != null) {
            List<Inventory> inventoryList = inventoryRepository.findByRetailerId(retailerId);
            if (warehouseId != null) {
                inventoryList = inventoryList.stream()
                        .filter(inv -> inv.getWarehouse() != null && inv.getWarehouse().getId().equals(warehouseId))
                        .collect(Collectors.toList());
            }
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), inventoryList.size());
            List<Inventory> pagedList = start < inventoryList.size() ? inventoryList.subList(start, end) : List.of();
            inventory = new PageImpl<>(pagedList, pageable, inventoryList.size());
        } else if (warehouseId != null) {
            List<Inventory> inventoryList = inventoryRepository.findByWarehouseId(warehouseId);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), inventoryList.size());
            List<Inventory> pagedList = start < inventoryList.size() ? inventoryList.subList(start, end) : List.of();
            inventory = new PageImpl<>(pagedList, pageable, inventoryList.size());
        } else {
            inventory = inventoryRepository.findAll(pageable);
        }
        
        return inventory.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        return mapToResponse(inventory);
    }
    
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductAndRetailerAndWarehouse(Long productId, Long retailerId, Long warehouseId) {
        Inventory inventory = inventoryRepository.findByProductIdAndRetailerIdAndWarehouseId(productId, retailerId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", 
                        "productId=" + productId + ", retailerId=" + retailerId + ", warehouseId=" + warehouseId));
        return mapToResponse(inventory);
    }
    
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProduct(Long productId) {
        List<Inventory> inventoryList = inventoryRepository.findByProductId(productId);
        return inventoryList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getLowStockItems(Long retailerId, Long warehouseId, Pageable pageable) {
        Page<Inventory> inventory = inventoryRepository.findLowStockItems(retailerId, warehouseId, pageable);
        return inventory.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getItemsAtReorderPoint(Long retailerId, Long warehouseId, Pageable pageable) {
        Page<Inventory> inventory = inventoryRepository.findItemsAtReorderPoint(retailerId, warehouseId, pageable);
        return inventory.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public Long getTotalAvailableQuantity(Long productId, Long retailerId) {
        Long total = inventoryRepository.getTotalAvailableQuantity(productId, retailerId);
        return total != null ? total : 0L;
    }
    
    @Transactional
    public InventoryResponse createInventory(InventoryRequest request) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        // Validate retailer if provided
        Retailer retailer = null;
        if (request.getRetailerId() != null) {
            retailer = retailerRepository.findById(request.getRetailerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", request.getRetailerId()));
        }
        
        // Validate warehouse if provided
        Warehouse warehouse = null;
        if (request.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", request.getWarehouseId()));
        }
        
        // Check if inventory already exists
        Optional<Inventory> existingInventory = inventoryRepository.findByProductIdAndRetailerIdAndWarehouseId(
                request.getProductId(), request.getRetailerId(), request.getWarehouseId());
        
        if (existingInventory.isPresent()) {
            throw new BadRequestException("Inventory already exists for this product, retailer, and warehouse combination");
        }
        
        Inventory inventory = Inventory.builder()
                .product(product)
                .retailer(retailer)
                .warehouse(warehouse)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .reservedQuantity(request.getReservedQuantity() != null ? request.getReservedQuantity() : 0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .reorderPoint(request.getReorderPoint() != null ? request.getReorderPoint() : 0)
                .build();
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        log.info("Inventory created with ID: {}", savedInventory.getId());
        
        return mapToResponse(savedInventory);
    }
    
    @Transactional
    public InventoryResponse updateInventory(Long id, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        
        if (request.getQuantity() != null) {
            inventory.setQuantity(request.getQuantity());
        }
        
        if (request.getReservedQuantity() != null) {
            // Validate reserved quantity doesn't exceed quantity
            if (request.getReservedQuantity() > inventory.getQuantity()) {
                throw new BadRequestException("Reserved quantity cannot exceed total quantity");
            }
            inventory.setReservedQuantity(request.getReservedQuantity());
        }
        
        if (request.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }
        
        if (request.getReorderPoint() != null) {
            inventory.setReorderPoint(request.getReorderPoint());
        }
        
        Inventory updatedInventory = inventoryRepository.save(inventory);
        log.info("Inventory updated with ID: {}", updatedInventory.getId());
        
        return mapToResponse(updatedInventory);
    }
    
    @Transactional
    public InventoryResponse reserveInventory(ReserveInventoryRequest request, Long userId) {
        Inventory inventory = findOrCreateInventory(request.getProductId(), request.getRetailerId(), request.getWarehouseId());
        
        int newReservedQuantity = inventory.getReservedQuantity() + request.getQuantity();
        
        // Validate available quantity
        if (newReservedQuantity > inventory.getQuantity()) {
            throw new BadRequestException("Insufficient available quantity. Available: " + 
                    (inventory.getQuantity() - inventory.getReservedQuantity()) + ", Requested: " + request.getQuantity());
        }
        
        inventory.setReservedQuantity(newReservedQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        // Create stock movement record
        createStockMovement(updatedInventory, "OUT", request.getQuantity(), "RESERVATION", null, 
                "Reserved for order", userId);
        
        log.info("Inventory reserved: inventoryId={}, quantity={}", updatedInventory.getId(), request.getQuantity());
        
        return mapToResponse(updatedInventory);
    }
    
    @Transactional
    public InventoryResponse releaseInventory(ReleaseInventoryRequest request, Long userId) {
        Inventory inventory = findOrCreateInventory(request.getProductId(), request.getRetailerId(), request.getWarehouseId());
        
        int newReservedQuantity = inventory.getReservedQuantity() - request.getQuantity();
        
        if (newReservedQuantity < 0) {
            throw new BadRequestException("Cannot release more than reserved quantity. Reserved: " + 
                    inventory.getReservedQuantity() + ", Requested: " + request.getQuantity());
        }
        
        inventory.setReservedQuantity(newReservedQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        // Create stock movement record
        createStockMovement(updatedInventory, "RETURN", request.getQuantity(), "RELEASE", null, 
                "Released from reservation", userId);
        
        log.info("Inventory released: inventoryId={}, quantity={}", updatedInventory.getId(), request.getQuantity());
        
        return mapToResponse(updatedInventory);
    }
    
    @Transactional
    public InventoryResponse adjustInventory(Long id, Integer quantity, String notes, Long userId) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        
        int newQuantity = inventory.getQuantity() + quantity;
        
        if (newQuantity < 0) {
            throw new BadRequestException("Adjustment would result in negative quantity");
        }
        
        // If reducing quantity, also reduce reserved quantity if needed
        if (quantity < 0 && inventory.getReservedQuantity() > newQuantity) {
            inventory.setReservedQuantity(newQuantity);
        }
        
        inventory.setQuantity(newQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        
        // Create stock movement record
        String movementType = quantity > 0 ? "IN" : "OUT";
        createStockMovement(updatedInventory, movementType, Math.abs(quantity), "ADJUSTMENT", null, notes, userId);
        
        log.info("Inventory adjusted: inventoryId={}, quantity={}, newQuantity={}", 
                updatedInventory.getId(), quantity, newQuantity);
        
        return mapToResponse(updatedInventory);
    }
    
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
        
        // Check if inventory has stock movements
        if (inventory.getStockMovements() != null && !inventory.getStockMovements().isEmpty()) {
            throw new BadRequestException("Cannot delete inventory with stock movement history");
        }
        
        inventoryRepository.delete(inventory);
        log.info("Inventory deleted with ID: {}", id);
    }
    
    private Inventory findOrCreateInventory(Long productId, Long retailerId, Long warehouseId) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdAndRetailerIdAndWarehouseId(
                productId, retailerId, warehouseId);
        
        if (inventoryOpt.isPresent()) {
            return inventoryOpt.get();
        }
        
        // Create new inventory record
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Retailer retailer = null;
        if (retailerId != null) {
            retailer = retailerRepository.findById(retailerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
        }
        
        Warehouse warehouse = null;
        if (warehouseId != null) {
            warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));
        }
        
        Inventory inventory = Inventory.builder()
                .product(product)
                .retailer(retailer)
                .warehouse(warehouse)
                .quantity(0)
                .reservedQuantity(0)
                .lowStockThreshold(10)
                .reorderPoint(0)
                .build();
        
        return inventoryRepository.save(inventory);
    }
    
    private void createStockMovement(Inventory inventory, String movementType, Integer quantity, 
                                    String referenceType, Long referenceId, String notes, Long userId) {
        StockMovement stockMovement = StockMovement.builder()
                .inventory(inventory)
                .movementType(movementType)
                .quantity(quantity)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .notes(notes)
                .build();
        
        if (userId != null) {
            com.vendo.user.entity.User user = new com.vendo.user.entity.User();
            user.setId(userId);
            stockMovement.setCreatedBy(user);
        }
        
        stockMovementRepository.save(stockMovement);
    }
    
    private InventoryResponse mapToResponse(Inventory inventory) {
        InventoryResponse.InventoryResponseBuilder builder = InventoryResponse.builder()
                .id(inventory.getId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .lowStockThreshold(inventory.getLowStockThreshold())
                .reorderPoint(inventory.getReorderPoint())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt());
        
        if (inventory.getProduct() != null) {
            builder.productId(inventory.getProduct().getId());
            builder.product(InventoryResponse.ProductInfo.builder()
                    .id(inventory.getProduct().getId())
                    .name(inventory.getProduct().getName())
                    .sku(inventory.getProduct().getSku())
                    .build());
        }
        
        if (inventory.getRetailer() != null) {
            builder.retailerId(inventory.getRetailer().getId());
            builder.retailer(InventoryResponse.RetailerInfo.builder()
                    .id(inventory.getRetailer().getId())
                    .storeName(inventory.getRetailer().getStoreName())
                    .build());
        }
        
        if (inventory.getWarehouse() != null) {
            builder.warehouseId(inventory.getWarehouse().getId());
            builder.warehouse(InventoryResponse.WarehouseInfo.builder()
                    .id(inventory.getWarehouse().getId())
                    .name(inventory.getWarehouse().getName())
                    .code(inventory.getWarehouse().getCode())
                    .build());
        }
        
        // Calculate low stock and reorder point flags
        boolean isLowStock = inventory.getAvailableQuantity() != null && 
                            inventory.getAvailableQuantity() <= inventory.getLowStockThreshold();
        boolean isAtReorderPoint = inventory.getAvailableQuantity() != null && 
                                   inventory.getAvailableQuantity() <= inventory.getReorderPoint();
        
        builder.isLowStock(isLowStock);
        builder.isAtReorderPoint(isAtReorderPoint);
        
        return builder.build();
    }
}

