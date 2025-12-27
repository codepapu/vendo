package com.vendo.b2b.repository;

import com.vendo.b2b.entity.RecurringOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringOrderRepository extends JpaRepository<RecurringOrder, Long> {
    
    List<RecurringOrder> findByBusinessAccountId(Long businessAccountId);
    
    Page<RecurringOrder> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    List<RecurringOrder> findByStatus(String status);
    
    @Query("SELECT ro FROM RecurringOrder ro WHERE ro.businessAccount.id = :businessAccountId AND ro.status = :status")
    List<RecurringOrder> findByBusinessAccountIdAndStatus(@Param("businessAccountId") Long businessAccountId, 
                                                          @Param("status") String status);
    
    @Query("SELECT ro FROM RecurringOrder ro WHERE ro.status = 'ACTIVE' AND ro.nextOrderDate <= :date")
    List<RecurringOrder> findRecurringOrdersDueForGeneration(@Param("date") LocalDate date);
    
    @Query("SELECT ro FROM RecurringOrder ro WHERE ro.status = 'ACTIVE' AND ro.nextOrderDate BETWEEN :startDate AND :endDate")
    List<RecurringOrder> findRecurringOrdersForNotification(@Param("startDate") LocalDate startDate, 
                                                             @Param("endDate") LocalDate endDate);
}

