package com.vendo.order.repository;

import com.vendo.order.entity.ShipmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentHistoryRepository extends JpaRepository<ShipmentHistory, Long> {
    
    List<ShipmentHistory> findByShipmentId(Long shipmentId);
    
    List<ShipmentHistory> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
}
