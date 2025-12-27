package com.vendo.b2b.repository;

import com.vendo.b2b.entity.Quote;
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
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    
    Optional<Quote> findByQuoteNumber(String quoteNumber);
    
    List<Quote> findByBusinessAccountId(Long businessAccountId);
    
    Page<Quote> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    List<Quote> findByStatus(String status);
    
    Page<Quote> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT q FROM Quote q WHERE q.businessAccount.id = :businessAccountId AND q.status = :status")
    List<Quote> findByBusinessAccountIdAndStatus(@Param("businessAccountId") Long businessAccountId, 
                                                  @Param("status") String status);
    
    @Query("SELECT q FROM Quote q WHERE q.expirationDate < :date AND q.status NOT IN ('EXPIRED', 'CONVERTED')")
    List<Quote> findExpiredQuotes(@Param("date") LocalDate date);
    
    @Query("SELECT q FROM Quote q WHERE q.createdBy.id = :userId")
    List<Quote> findByCreatedBy(@Param("userId") Long userId);
}

