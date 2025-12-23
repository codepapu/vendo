package com.vendo.inventory.repository;

import com.vendo.inventory.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    Optional<Warehouse> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Warehouse> findByIsActiveTrue();
    
    Page<Warehouse> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT w FROM Warehouse w WHERE w.isActive = true ORDER BY w.name ASC")
    List<Warehouse> findAllActiveWarehouses();
    
    @Query("SELECT w FROM Warehouse w WHERE w.isActive = :isActive AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Warehouse> searchWarehouses(@Param("keyword") String keyword, 
                                     @Param("isActive") Boolean isActive, 
                                     Pageable pageable);
}

