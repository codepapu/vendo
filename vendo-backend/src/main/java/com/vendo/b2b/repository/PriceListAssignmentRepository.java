package com.vendo.b2b.repository;

import com.vendo.b2b.entity.PriceListAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListAssignmentRepository extends JpaRepository<PriceListAssignment, Long> {
    
    @Query("SELECT pla FROM PriceListAssignment pla WHERE pla.businessAccount.id = :businessAccountId AND pla.isActive = true")
    List<PriceListAssignment> findActiveAssignmentsByBusinessAccountId(@Param("businessAccountId") Long businessAccountId);
    
    @Query("SELECT pla FROM PriceListAssignment pla WHERE pla.priceList.id = :priceListId AND pla.isActive = true")
    List<PriceListAssignment> findActiveAssignmentsByPriceListId(@Param("priceListId") Long priceListId);
    
    @Query("SELECT pla FROM PriceListAssignment pla WHERE pla.priceList.id = :priceListId AND pla.businessAccount.id = :businessAccountId")
    Optional<PriceListAssignment> findByPriceListIdAndBusinessAccountId(@Param("priceListId") Long priceListId, 
                                                                         @Param("businessAccountId") Long businessAccountId);
}

