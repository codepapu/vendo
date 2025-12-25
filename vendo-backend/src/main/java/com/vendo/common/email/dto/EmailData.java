package com.vendo.common.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Email Data DTO
 * Contains all data needed for email templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailData {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
    private String from;
    private String replyTo;
}

