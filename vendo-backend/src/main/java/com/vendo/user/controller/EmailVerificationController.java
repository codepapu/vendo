package com.vendo.user.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.user.dto.request.VerifyEmailRequest;
import com.vendo.user.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {
    
    private final EmailVerificationService emailVerificationService;
    
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }
    
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerificationEmail(@RequestParam String email) {
        emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Verification email sent successfully", null));
    }
}
