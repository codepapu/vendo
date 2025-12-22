package com.vendo.product.repository;

import com.vendo.product.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    Optional<Currency> findByCode(String code);
    
    Optional<Currency> findByIsDefaultTrue();
    
    List<Currency> findByStatus(String status);
    
    @org.springframework.data.jpa.repository.Query("SELECT c FROM Currency c WHERE c.status = 'published' ORDER BY c.order ASC")
    List<Currency> findPublishedCurrencies();
}
