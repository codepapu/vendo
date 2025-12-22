package com.vendo.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(max = 191, message = "Username must not exceed 191 characters")
    private String username;
    
    @Size(max = 191, message = "First name must not exceed 191 characters")
    private String firstName;
    
    @Size(max = 191, message = "Last name must not exceed 191 characters")
    private String lastName;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    private Boolean superUser;
    private Boolean isActivated;
}
