package com.chatapp.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String messageId;
    private String senderId;
    private String recipientId;
    private String content;
    private MessageType messageType;
    private LocalDateTime timestamp;
    private MessageStatus status;
    private Map<String, Object> metadata;
    private String replyToMessageId;
    private boolean isEdited;
    private LocalDateTime editedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum MessageType {
        TEXT, IMAGE, VIDEO, FILE, AUDIO, LOCATION, CONTACT, SYSTEM
    }

    public enum MessageStatus {
        SENDING, SENT, DELIVERED, SEEN, FAILED
    }
}
