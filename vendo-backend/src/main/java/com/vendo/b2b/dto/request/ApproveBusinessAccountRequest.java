package com.vendo.b2b.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveBusinessAccountRequest {
    
    @NotBlank(message = "Status is required")
    private String status; // APPROVED or REJECTED
    
    private String comments; // Optional comments for approval/rejection
}

