package com.vendo.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuthResponse {
    
    private Long id;
    
    private Long userId;
    
    private Boolean enabled;
    
    private String qrCodeUrl;
    
    private String secretKey; // Only returned during setup
    
    private List<String> backupCodes; // Only returned during setup
    
    private LocalDateTime verifiedAt;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private SetupInstructions setupInstructions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SetupInstructions {
        private String qrCodeUrl;
        private String manualEntryKey;
        private String issuer;
        private String accountName;
        private List<String> steps;
    }
}

