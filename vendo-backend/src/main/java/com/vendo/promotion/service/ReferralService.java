package com.vendo.promotion.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.promotion.dto.response.ReferralResponse;
import com.vendo.promotion.entity.Referral;
import com.vendo.promotion.entity.ReferralReward;
import com.vendo.promotion.repository.ReferralRepository;
import com.vendo.promotion.repository.ReferralRewardRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralService {
    
    private static final BigDecimal REFERRER_REWARD = BigDecimal.valueOf(10); // $10 for referrer
    private static final BigDecimal REFEREE_REWARD = BigDecimal.valueOf(5); // $5 for referee
    
    private final ReferralRepository referralRepository;
    private final ReferralRewardRepository referralRewardRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ReferralResponse generateReferralCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Referral existing = referralRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return mapToResponse(existing);
        }
        
        String referralCode = generateUniqueCode();
        Referral referral = Referral.builder()
                .user(user)
                .referralCode(referralCode)
                .totalReferrals(0)
                .totalConversions(0)
                .totalRewards(BigDecimal.ZERO)
                .isActive(true)
                .build();
        
        Referral saved = referralRepository.save(referral);
        log.info("Referral code generated: userId={}, code={}", userId, referralCode);
        return mapToResponse(saved);
    }
    
    @Transactional
    public ReferralResponse trackReferral(String referralCode, Long refereeUserId) {
        Referral referral = referralRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", "code", referralCode));
        
        if (referral.getUser().getId().equals(refereeUserId)) {
            throw new com.vendo.common.exception.BadRequestException("Cannot refer yourself");
        }
        
        // Check if already referred
        if (referralRepository.findByReferralCodeAndRefereeUserId(referralCode, refereeUserId).isPresent()) {
            throw new com.vendo.common.exception.BadRequestException("User already referred");
        }
        
        referral.setTotalReferrals(referral.getTotalReferrals() + 1);
        referralRepository.save(referral);
        
        log.info("Referral tracked: code={}, refereeUserId={}", referralCode, refereeUserId);
        return mapToResponse(referral);
    }
    
    @Transactional
    public ReferralResponse processReferralReward(String referralCode, Long refereeUserId, BigDecimal orderAmount) {
        Referral referral = referralRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", "code", referralCode));
        
        // Check if conversion already processed
        if (referralRewardRepository.findByReferralCodeAndRefereeUserId(referralCode, refereeUserId).isPresent()) {
            return mapToResponse(referral);
        }
        
        // Process rewards
        User referrer = referral.getUser();
        User referee = userRepository.findById(refereeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", refereeUserId));
        
        // Reward referrer
        ReferralReward referrerReward = ReferralReward.builder()
                .referral(referral)
                .user(referrer)
                .rewardType("REFERRER")
                .amount(REFERRER_REWARD)
                .description("Referral reward for referring user")
                .build();
        referralRewardRepository.save(referrerReward);
        
        // Reward referee
        ReferralReward refereeReward = ReferralReward.builder()
                .referral(referral)
                .user(referee)
                .rewardType("REFEREE")
                .amount(REFEREE_REWARD)
                .description("Welcome reward for using referral code")
                .build();
        referralRewardRepository.save(refereeReward);
        
        // Update referral stats
        referral.setTotalConversions(referral.getTotalConversions() + 1);
        referral.setTotalRewards(referral.getTotalRewards().add(REFERRER_REWARD).add(REFEREE_REWARD));
        referralRepository.save(referral);
        
        log.info("Referral rewards processed: code={}, referrer={}, referee={}", 
                referralCode, referrer.getId(), refereeUserId);
        return mapToResponse(referral);
    }
    
    @Transactional(readOnly = true)
    public ReferralResponse getReferralStats(Long userId) {
        Referral referral = referralRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", "userId", userId));
        return mapToResponse(referral);
    }
    
    private String generateUniqueCode() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private ReferralResponse mapToResponse(Referral referral) {
        return ReferralResponse.builder()
                .id(referral.getId())
                .userId(referral.getUser().getId())
                .referralCode(referral.getReferralCode())
                .totalReferrals(referral.getTotalReferrals())
                .totalConversions(referral.getTotalConversions())
                .totalRewards(referral.getTotalRewards())
                .isActive(referral.getIsActive())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }
}

