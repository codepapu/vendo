package com.vendo.retailer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionReportResponse {
    
    private Long retailerId;
    private String retailerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCommissions;
    private BigDecimal pendingCommissions;
    private BigDecimal paidCommissions;
    private BigDecimal settledCommissions;
    private Long totalTransactions;
    private List<CommissionSummary> summaries;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommissionSummary {
        private LocalDate date;
        private BigDecimal amount;
        private Long transactionCount;
        private String status;
    }
}

