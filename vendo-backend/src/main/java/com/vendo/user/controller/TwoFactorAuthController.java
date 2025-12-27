package com.vendo.user.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.user.dto.request.Verify2FARequest;
import com.vendo.user.dto.response.TwoFactorAuthResponse;
import com.vendo.user.service.TwoFactorAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/2fa")
@RequiredArgsConstructor
@Tag(name = "Two-Factor Authentication", description = "2FA management endpoints")
public class TwoFactorAuthController {
    
    private final TwoFactorAuthService twoFactorAuthService;
    
    @PostMapping("/enable")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Enable 2FA", description = "Initiate 2FA setup and get QR code")
    public ResponseEntity<ApiResponse<TwoFactorAuthResponse>> enable2FA(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        TwoFactorAuthResponse response = twoFactorAuthService.enable2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA setup initiated. Scan the QR code with your authenticator app.", response));
    }
    
    @PostMapping("/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Verify 2FA code", description = "Verify TOTP code and enable 2FA")
    public ResponseEntity<ApiResponse<TwoFactorAuthResponse>> verify2FA(
            @Valid @RequestBody Verify2FARequest request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        TwoFactorAuthResponse response = twoFactorAuthService.verify2FACode(userId, request);
        return ResponseEntity.ok(ApiResponse.success("2FA verified and enabled successfully", response));
    }
    
    @PostMapping("/disable")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Disable 2FA", description = "Disable 2FA for the current user")
    public ResponseEntity<ApiResponse<Void>> disable2FA(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        twoFactorAuthService.disable2FA(userId);
        return ResponseEntity.ok(ApiResponse.success("2FA disabled successfully"));
    }
    
    @PostMapping("/backup-codes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate backup codes", description = "Generate new backup codes for account recovery")
    public ResponseEntity<ApiResponse<TwoFactorAuthResponse>> generateBackupCodes(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        TwoFactorAuthResponse response = twoFactorAuthService.generateBackupCodes(userId);
        return ResponseEntity.ok(ApiResponse.success("New backup codes generated. Save them in a safe place.", response));
    }
    
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get 2FA status", description = "Get 2FA status for the current user")
    public ResponseEntity<ApiResponse<TwoFactorAuthResponse>> get2FAStatus(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        TwoFactorAuthResponse response = twoFactorAuthService.get2FAStatus(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

