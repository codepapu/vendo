package com.vendo.b2b.repository;

import com.vendo.b2b.entity.BusinessContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessContactRepository extends JpaRepository<BusinessContact, Long> {
    
    List<BusinessContact> findByBusinessAccountId(Long businessAccountId);
    
    List<BusinessContact> findByUserId(Long userId);
    
    @Query("SELECT bc FROM BusinessContact bc WHERE bc.businessAccount.id = :businessAccountId AND bc.isPrimary = true")
    Optional<BusinessContact> findPrimaryContactByBusinessAccountId(@Param("businessAccountId") Long businessAccountId);
    
    @Query("SELECT bc FROM BusinessContact bc WHERE bc.businessAccount.id = :businessAccountId AND bc.contactType = :contactType")
    List<BusinessContact> findByBusinessAccountIdAndContactType(@Param("businessAccountId") Long businessAccountId, 
                                                                 @Param("contactType") String contactType);
    
    @Query("SELECT bc FROM BusinessContact bc WHERE bc.businessAccount.id = :businessAccountId AND bc.user.id = :userId")
    Optional<BusinessContact> findByBusinessAccountIdAndUserId(@Param("businessAccountId") Long businessAccountId, 
                                                                @Param("userId") Long userId);
}

