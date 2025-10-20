package com.chatapp.notification.service;

import com.chatapp.common.dto.BaseResponse;
import com.chatapp.common.event.ChatEvent;
import com.chatapp.common.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final PushNotificationService pushNotificationService;

    @KafkaListener(topics = "message-events", groupId = "notification-service")
    public void handleMessageEvent(ChatEvent event) {
        log.info("Received message event: {}", event.getEventType());
        
        if (event.getEventType() == ChatEvent.EventType.MESSAGE_SENT) {
            Message message = (Message) event.getMessage();
            sendNotificationForMessage(message);
        }
    }

    private void sendNotificationForMessage(Message message) {
        try {
            // Check if user is online (would check presence service in real implementation)
            boolean isUserOnline = checkUserOnlineStatus(message.getRecipientId());
            
            if (!isUserOnline) {
                // Send push notification
                sendPushNotification(message);
                
                // Send email notification if user prefers
                if (shouldSendEmailNotification(message.getRecipientId())) {
                    sendEmailNotification(message);
                }
            }
        } catch (Exception e) {
            log.error("Error sending notification for message: {}", message.getMessageId(), e);
        }
    }

    private void sendPushNotification(Message message) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("messageId", message.getMessageId());
            data.put("senderId", message.getSenderId());
            data.put("content", message.getContent());
            data.put("type", "MESSAGE");

            pushNotificationService.sendPushNotification(
                message.getRecipientId(), 
                "New Message", 
                message.getContent(), 
                data
            );
            
            log.info("Push notification sent to user: {}", message.getRecipientId());
        } catch (Exception e) {
            log.error("Error sending push notification to user: {}", message.getRecipientId(), e);
        }
    }

    private void sendEmailNotification(Message message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(getUserEmail(message.getRecipientId()));
            email.setSubject("New Message Received");
            email.setText("You have received a new message: " + message.getContent());
            
            mailSender.send(email);
            
            log.info("Email notification sent to user: {}", message.getRecipientId());
        } catch (Exception e) {
            log.error("Error sending email notification to user: {}", message.getRecipientId(), e);
        }
    }

    private boolean checkUserOnlineStatus(String userId) {
        // In a real implementation, this would check the presence service
        // For now, return false to simulate offline user
        return false;
    }

    private boolean shouldSendEmailNotification(String userId) {
        // In a real implementation, this would check user preferences
        // For now, return true for demo purposes
        return true;
    }

    private String getUserEmail(String userId) {
        // In a real implementation, this would fetch from user service
        // For now, return a demo email
        return "user" + userId + "@example.com";
    }

    public BaseResponse<String> sendTestNotification(String userId, String title, String body) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", "TEST");

            pushNotificationService.sendPushNotification(userId, title, body, data);
            
            return BaseResponse.success("Test notification sent successfully");
        } catch (Exception e) {
            log.error("Error sending test notification to user: {}", userId, e);
            return BaseResponse.error("Failed to send test notification", "NOTIFICATION_FAILED");
        }
    }
}
