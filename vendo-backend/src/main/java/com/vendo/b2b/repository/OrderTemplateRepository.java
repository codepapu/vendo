package com.vendo.b2b.repository;

import com.vendo.b2b.entity.OrderTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTemplateRepository extends JpaRepository<OrderTemplate, Long> {
    
    List<OrderTemplate> findByBusinessAccountId(Long businessAccountId);
    
    Page<OrderTemplate> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    @Query("SELECT ot FROM OrderTemplate ot WHERE ot.businessAccount.id = :businessAccountId AND ot.isShared = true")
    List<OrderTemplate> findSharedTemplatesByBusinessAccountId(@Param("businessAccountId") Long businessAccountId);
    
    @Query("SELECT ot FROM OrderTemplate ot WHERE ot.createdBy.id = :userId")
    List<OrderTemplate> findByCreatedBy(@Param("userId") Long userId);
}

