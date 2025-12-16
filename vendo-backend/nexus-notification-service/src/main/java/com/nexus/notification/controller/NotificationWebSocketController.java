package com.nexus.notification.controller;

import com.nexus.notification.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for real-time notifications.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Send notification to specific user via WebSocket.
     * 
     * @param userId The user ID
     * @param notification The notification to send
     */
    public void sendNotificationToUser(Long userId, NotificationResponse notification) {
        String destination = "/topic/user/" + userId + "/notifications";
        messagingTemplate.convertAndSend(destination, notification);
        log.debug("WebSocket notification sent to user {}: {}", userId, notification.getId());
    }
    
    /**
     * Broadcast notification to all connected users.
     * 
     * @param notification The notification to broadcast
     */
    @MessageMapping("/notifications/broadcast")
    @SendTo("/topic/notifications")
    public NotificationResponse broadcastNotification(NotificationResponse notification) {
        log.info("Broadcasting notification: {}", notification.getId());
        return notification;
    }
}
