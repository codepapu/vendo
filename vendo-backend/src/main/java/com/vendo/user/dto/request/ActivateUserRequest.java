package com.vendo.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateUserRequest {
    
    @NotBlank(message = "Activation code is required")
    private String code;
}
