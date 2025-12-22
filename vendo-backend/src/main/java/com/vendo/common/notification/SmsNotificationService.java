package com.vendo.common.notification;

import com.vendo.common.sms.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    
    private final SmsService smsService;
    
    public void send(String phoneNumber, String message, Map<String, Object> data) {
        try {
            // Replace template variables
            String finalMessage = message;
            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    finalMessage = finalMessage.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
                }
            }
            
            smsService.sendSms(phoneNumber, finalMessage);
            log.info("SMS notification sent to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS notification", e);
        }
    }
}
