package com.vendo.marketing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMarketingResponse {
    
    private String syncStatus; // SUCCESS, FAILED, PENDING
    private Integer syncedCount;
    private String provider; // MAILCHIMP, SENDGRID, CONSTANT_CONTACT
    private String campaignId;
    private CampaignMetrics metrics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignMetrics {
        private String campaignId;
        private Integer sent;
        private Integer delivered;
        private Integer opened;
        private Integer clicked;
        private Integer bounced;
        private Integer unsubscribed;
        private Double openRate;
        private Double clickRate;
    }
}

