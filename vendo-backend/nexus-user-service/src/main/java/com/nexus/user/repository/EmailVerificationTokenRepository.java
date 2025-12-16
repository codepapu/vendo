package com.nexus.user.repository;

import com.nexus.user.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    
    @Query("SELECT e FROM EmailVerificationToken e WHERE e.user.id = ?1 AND e.verifiedAt IS NULL")
    Optional<EmailVerificationToken> findByUserIdAndVerifiedAtIsNull(Long userId);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.expiresAt < ?1")
    void deleteExpiredTokens(LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM EmailVerificationToken e WHERE e.user.id = ?1")
    void deleteByUserId(Long userId);
}
