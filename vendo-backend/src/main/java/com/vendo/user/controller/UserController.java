package com.vendo.user.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.user.dto.response.AuthResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import com.vendo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserRepository userRepository;
    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse.UserDto>> getProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.vendo.common.exception.ResourceNotFoundException("User", "id", userId));
        
        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .superUser(user.getSuperUser())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(userDto));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse.UserDto>> updateProfile(
            @RequestBody com.vendo.user.dto.request.UpdateProfileRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        AuthResponse.UserDto updatedUser = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
    }
}
