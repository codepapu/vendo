package com.vendo.tax.repository;

import com.vendo.tax.entity.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {
    
    List<TaxRule> findByIsActiveTrueOrderByPriorityAsc();
    
    List<TaxRule> findByRuleTypeAndIsActiveTrueOrderByPriorityAsc(String ruleType);
    
    @Query("SELECT t FROM TaxRule t WHERE t.isActive = true " +
           "AND (t.country = :country OR t.country IS NULL) " +
           "AND (t.state = :state OR t.state IS NULL) " +
           "AND (t.city = :city OR t.city IS NULL) " +
           "AND t.ruleType = 'LOCATION' " +
           "ORDER BY t.priority ASC")
    List<TaxRule> findLocationBasedRules(
        @Param("country") String country,
        @Param("state") String state,
        @Param("city") String city
    );
    
    @Query("SELECT t FROM TaxRule t WHERE t.isActive = true " +
           "AND t.productType = :productType " +
           "AND t.ruleType = 'PRODUCT_TYPE' " +
           "ORDER BY t.priority ASC")
    List<TaxRule> findByProductType(@Param("productType") String productType);
    
    @Query("SELECT t FROM TaxRule t WHERE t.isActive = true " +
           "AND t.businessType = :businessType " +
           "AND t.ruleType = 'BUSINESS_TYPE' " +
           "ORDER BY t.priority ASC")
    List<TaxRule> findByBusinessType(@Param("businessType") String businessType);
    
    @Query("SELECT t FROM TaxRule t WHERE t.isActive = true " +
           "AND t.ruleType = 'DEFAULT' " +
           "ORDER BY t.priority ASC")
    List<TaxRule> findDefaultRules();
}

