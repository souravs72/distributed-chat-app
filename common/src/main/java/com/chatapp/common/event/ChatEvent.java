package com.chatapp.common.event;

import com.chatapp.common.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {
    private String eventId;
    private EventType eventType;
    private String userId;
    private String targetUserId;
    private Message message;
    private Object payload;
    private LocalDateTime timestamp;

    public enum EventType {
        MESSAGE_SENT,
        MESSAGE_DELIVERED,
        MESSAGE_SEEN,
        USER_TYPING,
        USER_STOPPED_TYPING,
        USER_ONLINE,
        USER_OFFLINE,
        USER_STATUS_CHANGED,
        MESSAGE_EDITED,
        MESSAGE_DELETED
    }
}
