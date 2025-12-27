package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResponse {
    
    private Long importId;
    private Long retailerId;
    private String fileName;
    private String fileType;
    private Integer totalRecords;
    private Integer successCount;
    private Integer errorCount;
    private String status;
    private List<ImportError> errors;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportError {
        private Integer rowNumber;
        private String field;
        private String message;
        private String data;
    }
}

