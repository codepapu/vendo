package com.vendo.user.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.user.UserUpdatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.content.dto.response.MediaFileResponse;
import com.vendo.content.entity.MediaFile;
import com.vendo.content.repository.MediaFileRepository;
import com.vendo.content.service.MediaService;
import com.vendo.notification.dto.response.NotificationPreferenceResponse;
import com.vendo.notification.service.NotificationPreferenceService;
import com.vendo.user.dto.request.ChangePasswordRequest;
import com.vendo.user.dto.request.UpdateProfileRequest;
import com.vendo.user.dto.response.UserProfileResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {
    
    private final UserRepository userRepository;
    private final MediaFileRepository mediaFileRepository;
    private final MediaService mediaService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        return buildProfileResponse(user);
    }
    
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate and update email
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            validateEmail(request.getEmail());
            user.setEmail(request.getEmail());
        }
        
        // Validate and update username
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            validateUsername(request.getUsername());
            user.setUsername(request.getUsername());
        }
        
        // Update other fields
        if (request.getFirstName() != null) {
            validateName(request.getFirstName(), "First name");
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            validateName(request.getLastName(), "Last name");
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            validatePhone(request.getPhone());
            user.setPhone(request.getPhone());
        }
        
        user = userRepository.save(user);
        
        // Publish UserUpdatedEvent
        Map<String, Object> updatedFields = new HashMap<>();
        if (request.getFirstName() != null) updatedFields.put("firstName", request.getFirstName());
        if (request.getLastName() != null) updatedFields.put("lastName", request.getLastName());
        if (request.getEmail() != null) updatedFields.put("email", request.getEmail());
        if (request.getUsername() != null) updatedFields.put("username", request.getUsername());
        if (request.getPhone() != null) updatedFields.put("phone", request.getPhone());
        
        if (!updatedFields.isEmpty()) {
            UserUpdatedEvent userUpdatedEvent = new UserUpdatedEvent(
                    user.getId(),
                    user.getEmail(),
                    updatedFields
            );
            eventPublisher.publish(userUpdatedEvent, "user-updated");
        }
        
        log.info("Profile updated for user ID: {}", userId);
        return buildProfileResponse(user);
    }
    
    @Transactional
    public void updatePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Validate new password
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }
        
        validatePassword(request.getNewPassword());
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password updated for user ID: {}", userId);
    }
    
    @Transactional
    public UserProfileResponse uploadAvatar(Long userId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Avatar file is required");
        }
        
        // Validate file size
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BadRequestException("Avatar file size must be less than 5MB");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new BadRequestException("Avatar must be an image file (JPEG, PNG, GIF, or WebP)");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Delete old avatar if exists
        if (user.getAvatarId() != null) {
            try {
                mediaService.deleteFile(user.getAvatarId());
            } catch (Exception e) {
                log.warn("Failed to delete old avatar: {}", user.getAvatarId(), e);
            }
        }
        
        // Upload new avatar
        MediaFileResponse mediaFileResponse = mediaService.uploadFile(file, userId);
        
        // Update user with new avatar ID
        user.setAvatarId(mediaFileResponse.getId());
        user = userRepository.save(user);
        
        log.info("Avatar uploaded for user ID: {}, media file ID: {}", userId, mediaFileResponse.getId());
        return buildProfileResponse(user);
    }
    
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Delete avatar if exists
        if (user.getAvatarId() != null) {
            try {
                mediaService.deleteFile(user.getAvatarId());
            } catch (Exception e) {
                log.warn("Failed to delete avatar during account deletion: {}", user.getAvatarId(), e);
            }
        }
        
        // Soft delete: deactivate account instead of hard delete
        // This preserves data integrity for orders, reviews, etc.
        user.setIsActive(false);
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        user.setUsername(user.getUsername() + "_deleted_" + System.currentTimeMillis());
        userRepository.save(user);
        
        log.info("Account deleted (deactivated) for user ID: {}", userId);
    }
    
    private UserProfileResponse buildProfileResponse(User user) {
        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());
        
        // Load avatar if exists
        if (user.getAvatarId() != null) {
            try {
                MediaFile avatarFile = mediaFileRepository.findById(user.getAvatarId())
                        .orElse(null);
                if (avatarFile != null) {
                    MediaFileResponse avatarResponse = MediaFileResponse.builder()
                            .id(avatarFile.getId())
                            .filename(avatarFile.getFilename())
                            .originalName(avatarFile.getOriginalName())
                            .url(avatarFile.getUrl())
                            .cdnUrl(avatarFile.getCdnUrl())
                            .width(avatarFile.getWidth())
                            .height(avatarFile.getHeight())
                            .build();
                    builder.avatar(avatarResponse);
                    builder.avatarUrl(avatarFile.getCdnUrl() != null ? avatarFile.getCdnUrl() : avatarFile.getUrl());
                }
            } catch (Exception e) {
                log.warn("Failed to load avatar for user ID: {}", user.getId(), e);
            }
        }
        
        // Load notification preferences
        try {
            NotificationPreferenceResponse preferences = notificationPreferenceService.getPreferences(user.getId());
            builder.notificationPreferences(preferences);
        } catch (Exception e) {
            log.warn("Failed to load notification preferences for user ID: {}", user.getId(), e);
        }
        
        return builder.build();
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Invalid email format");
        }
        if (email.length() > 255) {
            throw new BadRequestException("Email must be less than 255 characters");
        }
    }
    
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (username.length() < 3 || username.length() > 60) {
            throw new BadRequestException("Username must be between 3 and 60 characters");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BadRequestException("Username can only contain letters, numbers, and underscores");
        }
    }
    
    private void validateName(String name, String fieldName) {
        if (name != null && name.length() > 100) {
            throw new BadRequestException(fieldName + " must be less than 100 characters");
        }
    }
    
    private void validatePhone(String phone) {
        if (phone != null && phone.length() > 20) {
            throw new BadRequestException("Phone number must be less than 20 characters");
        }
        // Basic phone validation - can be enhanced
        if (phone != null && !phone.matches("^[+]?[0-9\\s\\-()]+$")) {
            throw new BadRequestException("Invalid phone number format");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (password.length() > 128) {
            throw new BadRequestException("Password must be less than 128 characters");
        }
    }
    
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
