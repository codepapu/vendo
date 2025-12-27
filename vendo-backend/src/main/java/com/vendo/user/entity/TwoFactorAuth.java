package com.vendo.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_auth", indexes = {
    @Index(name = "idx_2fa_user_id", columnList = "user_id"),
    @Index(name = "idx_2fa_enabled", columnList = "enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TwoFactorAuth {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;
    
    @Column(name = "secret_key", length = 255)
    private String secretKey;
    
    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes; // JSON array of hashed backup codes
    
    @Column(name = "qr_code_url", columnDefinition = "TEXT")
    private String qrCodeUrl;
    
    @Column(name = "last_used_backup_code")
    private LocalDateTime lastUsedBackupCode;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

