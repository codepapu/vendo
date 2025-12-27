package com.vendo.inventory.repository;

import com.vendo.inventory.entity.AutoReorderSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoReorderSettingsRepository extends JpaRepository<AutoReorderSettings, Long> {
    
    Optional<AutoReorderSettings> findByProductIdAndRetailerIdAndWarehouseId(
            Long productId, Long retailerId, Long warehouseId);
    
    List<AutoReorderSettings> findByIsEnabledTrue();
    
    @Query("SELECT a FROM AutoReorderSettings a WHERE a.isEnabled = true AND " +
           "EXISTS (SELECT i FROM Inventory i WHERE i.product.id = a.product.id AND " +
           "i.retailer.id = COALESCE(:retailerId, i.retailer.id) AND " +
           "i.warehouse.id = COALESCE(:warehouseId, i.warehouse.id) AND " +
           "i.availableQuantity <= a.reorderPoint)")
    List<AutoReorderSettings> findSettingsNeedingReorder(
            @Param("retailerId") Long retailerId,
            @Param("warehouseId") Long warehouseId);
}

