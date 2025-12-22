package com.vendo.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final PushNotificationService pushNotificationService;
    
    /**
     * Send notification via all channels
     */
    public void sendNotification(NotificationRequest request) {
        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            // Default: send via all channels
            sendEmail(request);
            sendSms(request);
            sendPush(request);
        } else {
            for (NotificationChannel channel : request.getChannels()) {
                switch (channel) {
                    case EMAIL:
                        sendEmail(request);
                        break;
                    case SMS:
                        sendSms(request);
                        break;
                    case PUSH:
                        sendPush(request);
                        break;
                }
            }
        }
    }
    
    /**
     * Send email notification
     */
    public void sendEmail(NotificationRequest request) {
        emailNotificationService.send(
                request.getRecipient(),
                request.getSubject(),
                request.getTemplate(),
                request.getData()
        );
    }
    
    /**
     * Send SMS notification
     */
    public void sendSms(NotificationRequest request) {
        smsNotificationService.send(
                request.getRecipient(),
                request.getMessage(),
                request.getData()
        );
    }
    
    /**
     * Send push notification
     */
    public void sendPush(NotificationRequest request) {
        pushNotificationService.send(
                request.getRecipient(),
                request.getTitle(),
                request.getMessage(),
                request.getData()
        );
    }
    
    /**
     * Send bulk notifications
     */
    public void sendBulkNotifications(List<NotificationRequest> requests) {
        requests.forEach(this::sendNotification);
    }
}
