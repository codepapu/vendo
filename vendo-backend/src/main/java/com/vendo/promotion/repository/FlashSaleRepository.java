package com.vendo.promotion.repository;

import com.vendo.promotion.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
    
    @Query("SELECT fs FROM FlashSale fs WHERE fs.isActive = true AND " +
           ":now >= fs.startTime AND :now <= fs.endTime")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);
    
    List<FlashSale> findByProductId(Long productId);
}

