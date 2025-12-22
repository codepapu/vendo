package com.vendo.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class PushNotificationService {
    
    public void send(String recipient, String title, String message, Map<String, Object> data) {
        try {
            // TODO: Implement push notification (FCM/APNS)
            // For now, just log
            log.info("Push notification would be sent to: {} with title: {}", recipient, title);
            log.debug("Push notification data: {}", data);
        } catch (Exception e) {
            log.error("Failed to send push notification to: {}", recipient, e);
            throw new RuntimeException("Failed to send push notification", e);
        }
    }
}
