package com.vendo.promotion.repository;

import com.vendo.promotion.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
    @Query("SELECT t FROM LoyaltyTransaction t WHERE t.loyaltyAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<LoyaltyTransaction> findByLoyaltyAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId);
}

