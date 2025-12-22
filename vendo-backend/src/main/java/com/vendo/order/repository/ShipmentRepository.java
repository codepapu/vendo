package com.vendo.order.repository;

import com.vendo.order.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    List<Shipment> findByOrderId(Long orderId);
    
    Optional<Shipment> findByTrackingId(String trackingId);
    
    List<Shipment> findByStatus(String status);
    
    @Query("SELECT s FROM Shipment s WHERE s.order.id = :orderId ORDER BY s.createdAt DESC")
    List<Shipment> findShipmentsByOrderId(@Param("orderId") Long orderId);
}
