package com.chatapp.websocket.controller;

import com.chatapp.common.event.ChatEvent;
import com.chatapp.common.model.Message;
import com.chatapp.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;

    @MessageMapping("/chat.sendMessage")
    @SendToUser("/queue/messages")
    public void sendMessage(@Payload Message message, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getUser().getName();
        log.info("User {} sending message to {}", userId, message.getRecipientId());
        webSocketService.sendMessage(message);
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingMessage typingMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getUser().getName();
        log.info("User {} typing to {}", userId, typingMessage.getTargetUserId());
        webSocketService.handleTyping(userId, typingMessage.getTargetUserId(), typingMessage.isTyping());
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload JoinChatMessage joinMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getUser().getName();
        log.info("User {} joining chat with {}", userId, joinMessage.getTargetUserId());
        webSocketService.joinChat(userId, joinMessage.getTargetUserId());
    }

    @MessageMapping("/chat.leave")
    public void leaveChat(@Payload LeaveChatMessage leaveMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getUser().getName();
        log.info("User {} leaving chat with {}", userId, leaveMessage.getTargetUserId());
        webSocketService.leaveChat(userId, leaveMessage.getTargetUserId());
    }

    @MessageMapping("/presence.update")
    public void updatePresence(@Payload PresenceMessage presenceMessage, SimpMessageHeaderAccessor headerAccessor) {
        String userId = headerAccessor.getUser().getName();
        log.info("User {} updating presence: {}", userId, presenceMessage.isOnline());
        webSocketService.updatePresence(userId, presenceMessage.isOnline());
    }

    // DTO classes
    public static class TypingMessage {
        private String targetUserId;
        private boolean typing;

        public String getTargetUserId() { return targetUserId; }
        public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }
    }

    public static class JoinChatMessage {
        private String targetUserId;

        public String getTargetUserId() { return targetUserId; }
        public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
    }

    public static class LeaveChatMessage {
        private String targetUserId;

        public String getTargetUserId() { return targetUserId; }
        public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
    }

    public static class PresenceMessage {
        private boolean online;

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
}
