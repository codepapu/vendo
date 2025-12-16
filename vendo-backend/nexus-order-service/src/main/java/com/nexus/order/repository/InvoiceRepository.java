package com.nexus.order.repository;

import com.nexus.order.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByCode(String code);
    
    List<Invoice> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    List<Invoice> findByStatus(String status);
    
    @Query("SELECT i FROM Invoice i WHERE i.referenceType = :referenceType AND i.referenceId = :referenceId ORDER BY i.createdAt DESC")
    List<Invoice> findByReference(@Param("referenceType") String referenceType, @Param("referenceId") Long referenceId);
}
