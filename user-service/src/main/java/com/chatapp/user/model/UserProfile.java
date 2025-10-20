package com.chatapp.user.model;

import com.chatapp.common.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column
    private String displayName;

    @Column
    private String avatarUrl;

    @Column
    private String status;

    @Column
    private LocalDateTime lastSeen;

    @Column
    private boolean isOnline;

    @Column
    private boolean showLastSeen;

    @Column
    private boolean showProfilePhoto;

    @Column
    private boolean showStatus;

    @Column
    private boolean showReadReceipts;

    @Column
    private boolean allowMessagesFromUnknown;

    @Column
    private String lastSeenPolicy;

    @ElementCollection
    @CollectionTable(name = "user_contacts", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "contact_id")
    private List<String> contacts;

    @ElementCollection
    @CollectionTable(name = "user_blocked", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "blocked_user_id")
    private List<String> blockedUsers;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User.PrivacySettings getPrivacySettings() {
        return User.PrivacySettings.builder()
                .showLastSeen(showLastSeen)
                .showProfilePhoto(showProfilePhoto)
                .showStatus(showStatus)
                .showReadReceipts(showReadReceipts)
                .allowMessagesFromUnknown(allowMessagesFromUnknown)
                .lastSeenPolicy(lastSeenPolicy)
                .build();
    }

    public void setPrivacySettings(User.PrivacySettings privacySettings) {
        this.showLastSeen = privacySettings.isShowLastSeen();
        this.showProfilePhoto = privacySettings.isShowProfilePhoto();
        this.showStatus = privacySettings.isShowStatus();
        this.showReadReceipts = privacySettings.isShowReadReceipts();
        this.allowMessagesFromUnknown = privacySettings.isAllowMessagesFromUnknown();
        this.lastSeenPolicy = privacySettings.getLastSeenPolicy();
    }
}
