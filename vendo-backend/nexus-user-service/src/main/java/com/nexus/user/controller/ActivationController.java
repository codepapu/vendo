package com.nexus.user.controller;

import com.nexus.common.dto.ApiResponse;
import com.nexus.user.dto.request.ActivateUserRequest;
import com.nexus.user.service.ActivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/activation")
@RequiredArgsConstructor
public class ActivationController {
    
    private final ActivationService activationService;
    
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@Valid @RequestBody ActivateUserRequest request) {
        activationService.activateUser(request.getCode());
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully", null));
    }
    
    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Void>> resendActivationCode(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        activationService.resendActivationCode(userId);
        return ResponseEntity.ok(ApiResponse.success("Activation code sent successfully", null));
    }
}
