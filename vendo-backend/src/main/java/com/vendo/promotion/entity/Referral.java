package com.vendo.promotion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "referrals", indexes = {
    @Index(name = "idx_referrals_user_id", columnList = "user_id"),
    @Index(name = "idx_referrals_code", columnList = "referral_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Referral {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private com.vendo.user.entity.User user;
    
    @Column(name = "referral_code", nullable = false, unique = true, length = 50)
    private String referralCode;
    
    @Column(name = "total_referrals", nullable = false)
    @Builder.Default
    private Integer totalReferrals = 0;
    
    @Column(name = "total_conversions", nullable = false)
    @Builder.Default
    private Integer totalConversions = 0;
    
    @Column(name = "total_rewards", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRewards = BigDecimal.ZERO;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

