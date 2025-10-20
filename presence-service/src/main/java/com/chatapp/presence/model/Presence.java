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
public class Presence {
    private String userId;
    private boolean isOnline;
    private LocalDateTime lastSeen;
    private String status;
    private String deviceId;
    private LocalDateTime updatedAt;

    public enum Status {
        ONLINE, OFFLINE, AWAY, BUSY, INVISIBLE
    }
}
