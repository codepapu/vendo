package com.vendo.retailer.repository;

import com.vendo.retailer.entity.RetailerBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerBankAccountRepository extends JpaRepository<RetailerBankAccount, Long> {
    
    List<RetailerBankAccount> findByRetailerId(Long retailerId);
    
    Optional<RetailerBankAccount> findByRetailerIdAndIsPrimaryTrue(Long retailerId);
    
    List<RetailerBankAccount> findByRetailerIdAndIsPrimary(Long retailerId, Boolean isPrimary);
}

