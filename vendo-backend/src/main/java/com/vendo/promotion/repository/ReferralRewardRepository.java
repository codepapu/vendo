package com.vendo.promotion.repository;

import com.vendo.promotion.entity.ReferralReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralRewardRepository extends JpaRepository<ReferralReward, Long> {
    @Query("SELECT r FROM ReferralReward r WHERE r.referral.referralCode = :code AND r.user.id = :refereeUserId")
    Optional<ReferralReward> findByReferralCodeAndRefereeUserId(@Param("code") String code, 
                                                                  @Param("refereeUserId") Long refereeUserId);
}

