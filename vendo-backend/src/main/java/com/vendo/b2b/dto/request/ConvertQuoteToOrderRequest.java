package com.vendo.b2b.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertQuoteToOrderRequest {
    
    @NotNull(message = "Quote ID is required")
    private Long quoteId;
    
    private String notes; // Optional notes for the order
}

