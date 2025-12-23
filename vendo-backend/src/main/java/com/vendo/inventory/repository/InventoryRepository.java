package com.vendo.inventory.repository;

import com.vendo.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    Optional<Inventory> findByProductIdAndRetailerIdAndWarehouseId(Long productId, Long retailerId, Long warehouseId);
    
    List<Inventory> findByProductId(Long productId);
    
    List<Inventory> findByRetailerId(Long retailerId);
    
    List<Inventory> findByWarehouseId(Long warehouseId);
    
    List<Inventory> findByProductIdAndRetailerId(Long productId, Long retailerId);
    
    List<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
    
    List<Inventory> findByRetailerIdAndWarehouseId(Long retailerId, Long warehouseId);
    
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND " +
           "(:retailerId IS NULL OR i.retailer.id = :retailerId) AND " +
           "(:warehouseId IS NULL OR i.warehouse.id = :warehouseId)")
    List<Inventory> findByProductAndOptionalRetailerAndWarehouse(@Param("productId") Long productId,
                                                                  @Param("retailerId") Long retailerId,
                                                                  @Param("warehouseId") Long warehouseId);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.lowStockThreshold AND " +
           "(:retailerId IS NULL OR i.retailer.id = :retailerId) AND " +
           "(:warehouseId IS NULL OR i.warehouse.id = :warehouseId)")
    Page<Inventory> findLowStockItems(@Param("retailerId") Long retailerId,
                                      @Param("warehouseId") Long warehouseId,
                                      Pageable pageable);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.reorderPoint AND " +
           "(:retailerId IS NULL OR i.retailer.id = :retailerId) AND " +
           "(:warehouseId IS NULL OR i.warehouse.id = :warehouseId)")
    Page<Inventory> findItemsAtReorderPoint(@Param("retailerId") Long retailerId,
                                            @Param("warehouseId") Long warehouseId,
                                            Pageable pageable);
    
    @Query("SELECT SUM(i.availableQuantity) FROM Inventory i WHERE i.product.id = :productId AND " +
           "(:retailerId IS NULL OR i.retailer.id = :retailerId)")
    Long getTotalAvailableQuantity(@Param("productId") Long productId, 
                                   @Param("retailerId") Long retailerId);
    
    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.id = :productId AND " +
           "(:retailerId IS NULL OR i.retailer.id = :retailerId)")
    Long getTotalQuantity(@Param("productId") Long productId, 
                         @Param("retailerId") Long retailerId);
    
    @Query("SELECT SUM(i.reservedQuantity) FROM Inventory i WHERE i.product.id = :productId AND " +
           "(:retailerId IS NULL OR i.retailer.id = :retailerId)")
    Long getTotalReservedQuantity(@Param("productId") Long productId, 
                                  @Param("retailerId") Long retailerId);
}

