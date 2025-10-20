package com.chatapp.presence.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicator {
    private String userId;
    private String targetUserId;
    private boolean isTyping;
    private LocalDateTime timestamp;
    private String chatId;
}
