package com.nexus.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
    private String username;
    
    private String firstName;
    
    private String lastName;
    
    private String phone;
}
