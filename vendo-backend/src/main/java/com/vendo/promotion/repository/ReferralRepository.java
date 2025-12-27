package com.vendo.promotion.repository;

import com.vendo.promotion.entity.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    Optional<Referral> findByUserId(Long userId);
    Optional<Referral> findByReferralCode(String referralCode);
    
    @Query("SELECT r FROM Referral r WHERE r.referralCode = :code AND EXISTS " +
           "(SELECT u FROM User u WHERE u.id = :refereeUserId)")
    Optional<Referral> findByReferralCodeAndRefereeUserId(@Param("code") String code, 
                                                           @Param("refereeUserId") Long refereeUserId);
}

