package com.vendo.b2b.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringOrderRequest {
    
    @NotNull(message = "Business account ID is required")
    private Long businessAccountId;
    
    @NotNull(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Recurrence pattern is required")
    private String recurrencePattern; // DAILY, WEEKLY, MONTHLY, QUARTERLY
    
    @NotNull(message = "Recurrence value is required")
    private Integer recurrenceValue; // e.g., every 2 weeks
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer notificationDaysBefore; // notify X days before next order
    
    @NotEmpty(message = "Items are required")
    @Valid
    private List<RecurringOrderItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurringOrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}

