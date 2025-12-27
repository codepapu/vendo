package com.vendo.promotion.service;

import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.promotion.dto.request.LoyaltyRequest;
import com.vendo.promotion.dto.response.LoyaltyResponse;
import com.vendo.promotion.entity.LoyaltyAccount;
import com.vendo.promotion.entity.LoyaltyTransaction;
import com.vendo.promotion.repository.LoyaltyAccountRepository;
import com.vendo.promotion.repository.LoyaltyTransactionRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyService {
    
    private static final BigDecimal POINTS_PER_DOLLAR = BigDecimal.valueOf(1); // 1 point per dollar
    private static final BigDecimal REDEMPTION_RATE = BigDecimal.valueOf(100); // 100 points = $1
    
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public LoyaltyResponse enrollInLoyalty(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        LoyaltyAccount existing = loyaltyAccountRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return mapToResponse(existing);
        }
        
        LoyaltyAccount account = LoyaltyAccount.builder()
                .user(user)
                .pointsBalance(BigDecimal.ZERO)
                .tier("BRONZE")
                .isActive(true)
                .build();
        
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);
        log.info("User enrolled in loyalty program: userId={}", userId);
        return mapToResponse(saved);
    }
    
    @Transactional
    public LoyaltyResponse earnPoints(Long userId, BigDecimal purchaseAmount) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", "userId", userId));
        
        BigDecimal pointsEarned = purchaseAmount.multiply(POINTS_PER_DOLLAR);
        account.setPointsBalance(account.getPointsBalance().add(pointsEarned));
        
        // Update tier based on points
        updateTier(account);
        
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);
        
        // Create transaction
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .loyaltyAccount(saved)
                .transactionType("EARNED")
                .points(pointsEarned)
                .description("Points earned from purchase")
                .build();
        loyaltyTransactionRepository.save(transaction);
        
        log.info("Points earned: userId={}, points={}", userId, pointsEarned);
        return mapToResponse(saved);
    }
    
    @Transactional
    public LoyaltyResponse redeemPoints(Long userId, BigDecimal pointsToRedeem) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", "userId", userId));
        
        if (account.getPointsBalance().compareTo(pointsToRedeem) < 0) {
            throw new com.vendo.common.exception.BadRequestException("Insufficient points");
        }
        
        account.setPointsBalance(account.getPointsBalance().subtract(pointsToRedeem));
        LoyaltyAccount saved = loyaltyAccountRepository.save(account);
        
        // Create transaction
        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .loyaltyAccount(saved)
                .transactionType("REDEEMED")
                .points(pointsToRedeem.negate())
                .description("Points redeemed")
                .build();
        loyaltyTransactionRepository.save(transaction);
        
        log.info("Points redeemed: userId={}, points={}", userId, pointsToRedeem);
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public LoyaltyResponse getLoyaltyStatus(Long userId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", "userId", userId));
        return mapToResponse(account);
    }
    
    @Transactional(readOnly = true)
    public List<LoyaltyResponse.LoyaltyTransaction> getLoyaltyHistory(Long userId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyAccount", "userId", userId));
        
        return loyaltyTransactionRepository.findByLoyaltyAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(t -> LoyaltyResponse.LoyaltyTransaction.builder()
                        .id(t.getId())
                        .transactionType(t.getTransactionType())
                        .points(t.getPoints())
                        .description(t.getDescription())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
    
    private void updateTier(LoyaltyAccount account) {
        BigDecimal points = account.getPointsBalance();
        if (points.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            account.setTier("PLATINUM");
        } else if (points.compareTo(BigDecimal.valueOf(5000)) >= 0) {
            account.setTier("GOLD");
        } else if (points.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            account.setTier("SILVER");
        } else {
            account.setTier("BRONZE");
        }
    }
    
    private LoyaltyResponse mapToResponse(LoyaltyAccount account) {
        BigDecimal redeemableValue = account.getPointsBalance().divide(REDEMPTION_RATE, 2, java.math.RoundingMode.DOWN);
        
        return LoyaltyResponse.builder()
                .id(account.getId())
                .userId(account.getUser().getId())
                .pointsBalance(account.getPointsBalance())
                .tier(account.getTier())
                .redeemableValue(redeemableValue)
                .isActive(account.getIsActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}

