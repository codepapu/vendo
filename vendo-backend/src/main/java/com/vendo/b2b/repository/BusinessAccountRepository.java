package com.vendo.b2b.repository;

import com.vendo.b2b.entity.BusinessAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessAccountRepository extends JpaRepository<BusinessAccount, Long> {
    
    Optional<BusinessAccount> findByUserId(Long userId);
    
    List<BusinessAccount> findByStatus(String status);
    
    Page<BusinessAccount> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT ba FROM BusinessAccount ba WHERE ba.user.id = :userId AND ba.status = :status")
    List<BusinessAccount> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT ba FROM BusinessAccount ba WHERE ba.businessType = :businessType AND ba.status = :status")
    Page<BusinessAccount> findByBusinessTypeAndStatus(@Param("businessType") String businessType, 
                                                      @Param("status") String status, 
                                                      Pageable pageable);
    
    boolean existsByUserId(Long userId);
    
    boolean existsByCompanyName(String companyName);
}

