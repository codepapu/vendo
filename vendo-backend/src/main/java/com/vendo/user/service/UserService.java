package com.vendo.user.service;

import com.vendo.common.event.EventPublisher;
import com.vendo.common.event.user.UserUpdatedEvent;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.dto.request.UpdateProfileRequest;
import com.vendo.user.dto.response.AuthResponse;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public AuthResponse.UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        
        // Check username uniqueness if changed
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }
        
        // Update other fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
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
        
        return AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .superUser(user.getSuperUser())
                .build();
    }
}
