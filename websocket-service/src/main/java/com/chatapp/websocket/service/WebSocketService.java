package com.chatapp.websocket.service;

import com.chatapp.common.event.ChatEvent;
import com.chatapp.common.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // In-memory storage for active sessions (in production, use Redis)
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "message-events", groupId = "websocket-service")
    public void handleMessageEvent(ChatEvent event) {
        log.info("Received message event: {}", event.getEventType());
        
        switch (event.getEventType()) {
            case MESSAGE_SENT:
                handleMessageSent(event);
                break;
            case MESSAGE_DELIVERED:
                handleMessageDelivered(event);
                break;
            case MESSAGE_SEEN:
                handleMessageSeen(event);
                break;
            case MESSAGE_EDITED:
                handleMessageEdited(event);
                break;
            default:
                log.warn("Unhandled message event type: {}", event.getEventType());
        }
    }

    @KafkaListener(topics = "presence-events", groupId = "websocket-service")
    public void handlePresenceEvent(ChatEvent event) {
        log.info("Received presence event: {}", event.getEventType());
        
        switch (event.getEventType()) {
            case USER_ONLINE:
                handleUserOnline(event);
                break;
            case USER_OFFLINE:
                handleUserOffline(event);
                break;
            default:
                log.warn("Unhandled presence event type: {}", event.getEventType());
        }
    }

    @KafkaListener(topics = "typing-events", groupId = "websocket-service")
    public void handleTypingEvent(ChatEvent event) {
        log.info("Received typing event: {}", event.getEventType());
        
        switch (event.getEventType()) {
            case USER_TYPING:
                handleUserTyping(event);
                break;
            case USER_STOPPED_TYPING:
                handleUserStoppedTyping(event);
                break;
            default:
                log.warn("Unhandled typing event type: {}", event.getEventType());
        }
    }

    private void handleMessageSent(ChatEvent event) {
        Message message = (Message) event.getMessage();
        String recipientId = event.getTargetUserId();
        
        // Send to recipient if online
        if (isUserOnline(recipientId)) {
            messagingTemplate.convertAndSendToUser(
                recipientId, 
                "/queue/messages", 
                message
            );
            log.info("Message sent to online user: {}", recipientId);
        } else {
            log.info("User {} is offline, message will be delivered when online", recipientId);
        }
    }

    private void handleMessageDelivered(ChatEvent event) {
        Message message = (Message) event.getMessage();
        String senderId = event.getTargetUserId();
        
        // Send delivery confirmation to sender
        if (isUserOnline(senderId)) {
            messagingTemplate.convertAndSendToUser(
                senderId, 
                "/queue/message-status", 
                createStatusUpdate(message, "DELIVERED")
            );
            log.info("Delivery confirmation sent to sender: {}", senderId);
        }
    }

    private void handleMessageSeen(ChatEvent event) {
        Message message = (Message) event.getMessage();
        String senderId = event.getTargetUserId();
        
        // Send seen confirmation to sender
        if (isUserOnline(senderId)) {
            messagingTemplate.convertAndSendToUser(
                senderId, 
                "/queue/message-status", 
                createStatusUpdate(message, "SEEN")
            );
            log.info("Seen confirmation sent to sender: {}", senderId);
        }
    }

    private void handleMessageEdited(ChatEvent event) {
        Message message = (Message) event.getMessage();
        String recipientId = event.getTargetUserId();
        
        // Send edited message to recipient
        if (isUserOnline(recipientId)) {
            messagingTemplate.convertAndSendToUser(
                recipientId, 
                "/queue/message-updates", 
                message
            );
            log.info("Edited message sent to recipient: {}", recipientId);
        }
    }

    private void handleUserOnline(ChatEvent event) {
        String userId = event.getUserId();
        activeUsers.add(userId);
        
        // Notify contacts about user coming online
        messagingTemplate.convertAndSend("/topic/presence", createPresenceUpdate(userId, true));
        log.info("User {} is now online", userId);
    }

    private void handleUserOffline(ChatEvent event) {
        String userId = event.getUserId();
        activeUsers.remove(userId);
        
        // Notify contacts about user going offline
        messagingTemplate.convertAndSend("/topic/presence", createPresenceUpdate(userId, false));
        log.info("User {} is now offline", userId);
    }

    private void handleUserTyping(ChatEvent event) {
        String targetUserId = event.getTargetUserId();
        
        if (isUserOnline(targetUserId)) {
            messagingTemplate.convertAndSendToUser(
                targetUserId, 
                "/queue/typing", 
                createTypingUpdate(event.getUserId(), true)
            );
            log.info("Typing indicator sent to user: {}", targetUserId);
        }
    }

    private void handleUserStoppedTyping(ChatEvent event) {
        String targetUserId = event.getTargetUserId();
        
        if (isUserOnline(targetUserId)) {
            messagingTemplate.convertAndSendToUser(
                targetUserId, 
                "/queue/typing", 
                createTypingUpdate(event.getUserId(), false)
            );
            log.info("Stopped typing indicator sent to user: {}", targetUserId);
        }
    }

    public void sendMessage(Message message) {
        // This method is called when a user sends a message via WebSocket
        // The actual message sending is handled by the chat service
        log.info("Message received via WebSocket from {} to {}", 
                message.getSenderId(), message.getRecipientId());
    }

    public void handleTyping(String userId, String targetUserId, boolean isTyping) {
        // This method is called when a user starts/stops typing
        log.info("User {} {} typing to {}", userId, isTyping ? "started" : "stopped", targetUserId);
    }

    public void joinChat(String userId, String targetUserId) {
        activeUsers.add(userId);
        log.info("User {} joined chat with {}", userId, targetUserId);
    }

    public void leaveChat(String userId, String targetUserId) {
        activeUsers.remove(userId);
        log.info("User {} left chat with {}", userId, targetUserId);
    }

    public void updatePresence(String userId, boolean isOnline) {
        if (isOnline) {
            activeUsers.add(userId);
        } else {
            activeUsers.remove(userId);
        }
        
        // Store in Redis for persistence across instances
        String key = "user:online:" + userId;
        if (isOnline) {
            redisTemplate.opsForValue().set(key, userId, 5, TimeUnit.MINUTES);
        } else {
            redisTemplate.delete(key);
        }
        
        log.info("User {} presence updated: {}", userId, isOnline ? "online" : "offline");
    }

    private boolean isUserOnline(String userId) {
        return activeUsers.contains(userId) || redisTemplate.hasKey("user:online:" + userId);
    }

    private Object createStatusUpdate(Message message, String status) {
        return new Object() {
            public String getMessageId() { return message.getMessageId(); }
            public String getStatus() { return status; }
            public long getTimestamp() { return System.currentTimeMillis(); }
        };
    }

    private Object createPresenceUpdate(String userId, boolean isOnline) {
        return new Object() {
            public String getUserId() { return userId; }
            public boolean isOnline() { return isOnline; }
            public long getTimestamp() { return System.currentTimeMillis(); }
        };
    }

    private Object createTypingUpdate(String userId, boolean isTyping) {
        return new Object() {
            public String getUserId() { return userId; }
            public boolean isTyping() { return isTyping; }
            public long getTimestamp() { return System.currentTimeMillis(); }
        };
    }
}
