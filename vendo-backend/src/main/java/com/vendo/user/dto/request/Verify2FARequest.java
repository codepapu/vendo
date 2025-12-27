package com.vendo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verify2FARequest {
    
    @NotBlank(message = "2FA code is required")
    private String code;
    
    private String backupCode; // Alternative to TOTP code
}

