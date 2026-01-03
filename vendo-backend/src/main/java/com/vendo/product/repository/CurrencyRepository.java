package com.vendo.product.repository;

import com.vendo.product.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    @Query("SELECT c FROM Currency c WHERE c.symbol = :code")
    Optional<Currency> findByCode(@Param("code") String code);
    
    Optional<Currency> findByIsDefaultTrue();
    
    List<Currency> findByStatus(String status);
    
    @Query("SELECT c FROM Currency c WHERE c.status = 'published' ORDER BY c.order ASC")
    List<Currency> findPublishedCurrencies();
}
