package com.vendo.order.repository;

import com.vendo.order.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByOrderId(Long orderId);
    
    List<Invoice> findByBusinessAccountId(Long businessAccountId);
    
    List<Invoice> findByStatus(String status);
    
    Page<Invoice> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    Page<Invoice> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE i.businessAccount.id = :businessAccountId AND i.status = :status")
    List<Invoice> findByBusinessAccountIdAndStatus(@Param("businessAccountId") Long businessAccountId, 
                                                     @Param("status") String status);
    
    @Query("SELECT i FROM Invoice i WHERE i.order.id = :orderId ORDER BY i.createdAt DESC")
    List<Invoice> findByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status = 'PENDING'")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);
    
    @Query("SELECT i FROM Invoice i WHERE i.businessAccount.id = :businessAccountId AND i.dueDate < :date AND i.status = 'PENDING'")
    List<Invoice> findOverdueInvoicesByBusinessAccount(@Param("businessAccountId") Long businessAccountId, 
                                                         @Param("date") LocalDate date);
    
    @Query("SELECT i FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByIssueDateBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
}
