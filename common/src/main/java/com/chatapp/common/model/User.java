package com.chatapp.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String phoneNumber;
    private String displayName;
    private String avatarUrl;
    private LocalDateTime lastSeen;
    private boolean isOnline;
    private PrivacySettings privacySettings;
    private List<String> blockedUsers;
    private List<String> contacts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrivacySettings {
        private boolean showLastSeen;
        private boolean showProfilePhoto;
        private boolean showStatus;
        private boolean showReadReceipts;
        private boolean allowMessagesFromUnknown;
        private String lastSeenPolicy; // EVERYONE, CONTACTS, NOBODY
    }
}
