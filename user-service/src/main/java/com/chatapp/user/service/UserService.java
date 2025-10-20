package com.chatapp.user.service;

import com.chatapp.common.model.User;
import com.chatapp.common.dto.BaseResponse;
import com.chatapp.user.model.UserProfile;
import com.chatapp.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public BaseResponse<UserProfile> createUserProfile(String userId, String displayName) {
        if (userProfileRepository.existsByUserId(userId)) {
            return BaseResponse.error("User profile already exists", "PROFILE_EXISTS");
        }

        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .displayName(displayName)
                .isOnline(false)
                .showLastSeen(true)
                .showProfilePhoto(true)
                .showStatus(true)
                .showReadReceipts(true)
                .allowMessagesFromUnknown(false)
                .lastSeenPolicy("EVERYONE")
                .build();

        UserProfile savedProfile = userProfileRepository.save(profile);
        return BaseResponse.success(savedProfile, "User profile created successfully");
    }

    public BaseResponse<UserProfile> getUserProfile(String userId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }
        return BaseResponse.success(profile.get());
    }

    @Transactional
    public BaseResponse<UserProfile> updateUserProfile(String userId, UserProfile updatedProfile) {
        Optional<UserProfile> existingProfile = userProfileRepository.findByUserId(userId);
        if (existingProfile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }

        UserProfile profile = existingProfile.get();
        profile.setDisplayName(updatedProfile.getDisplayName());
        profile.setAvatarUrl(updatedProfile.getAvatarUrl());
        profile.setStatus(updatedProfile.getStatus());
        profile.setShowLastSeen(updatedProfile.isShowLastSeen());
        profile.setShowProfilePhoto(updatedProfile.isShowProfilePhoto());
        profile.setShowStatus(updatedProfile.isShowStatus());
        profile.setShowReadReceipts(updatedProfile.isShowReadReceipts());
        profile.setAllowMessagesFromUnknown(updatedProfile.isAllowMessagesFromUnknown());
        profile.setLastSeenPolicy(updatedProfile.getLastSeenPolicy());

        UserProfile savedProfile = userProfileRepository.save(profile);
        return BaseResponse.success(savedProfile, "User profile updated successfully");
    }

    @Transactional
    public BaseResponse<String> updatePresence(String userId, boolean isOnline) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }

        UserProfile userProfile = profile.get();
        userProfile.setOnline(isOnline);
        userProfile.setLastSeen(LocalDateTime.now());
        userProfileRepository.save(userProfile);

        return BaseResponse.success("Presence updated successfully");
    }

    @Transactional
    public BaseResponse<String> addContact(String userId, String contactId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }

        UserProfile userProfile = profile.get();
        if (!userProfile.getContacts().contains(contactId)) {
            userProfile.getContacts().add(contactId);
            userProfileRepository.save(userProfile);
        }

        return BaseResponse.success("Contact added successfully");
    }

    @Transactional
    public BaseResponse<String> removeContact(String userId, String contactId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }

        UserProfile userProfile = profile.get();
        userProfile.getContacts().remove(contactId);
        userProfileRepository.save(userProfile);

        return BaseResponse.success("Contact removed successfully");
    }

    @Transactional
    public BaseResponse<String> blockUser(String userId, String blockedUserId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }

        UserProfile userProfile = profile.get();
        if (!userProfile.getBlockedUsers().contains(blockedUserId)) {
            userProfile.getBlockedUsers().add(blockedUserId);
            userProfile.getContacts().remove(blockedUserId);
            userProfileRepository.save(userProfile);
        }

        return BaseResponse.success("User blocked successfully");
    }

    @Transactional
    public BaseResponse<String> unblockUser(String userId, String unblockedUserId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        if (profile.isEmpty()) {
            return BaseResponse.error("User profile not found", "PROFILE_NOT_FOUND");
        }

        UserProfile userProfile = profile.get();
        userProfile.getBlockedUsers().remove(unblockedUserId);
        userProfileRepository.save(userProfile);

        return BaseResponse.success("User unblocked successfully");
    }

    public BaseResponse<List<UserProfile>> getContacts(String userId) {
        List<UserProfile> contacts = userProfileRepository.findContactsByUserId(userId);
        return BaseResponse.success(contacts);
    }

    public BaseResponse<List<UserProfile>> searchUsers(String query) {
        List<UserProfile> users = userProfileRepository.searchByDisplayNameOrStatus(query);
        return BaseResponse.success(users);
    }

    public BaseResponse<List<User>> convertToUserList(List<String> userIds) {
        List<UserProfile> profiles = userProfileRepository.findByUserIds(userIds);
        List<User> users = profiles.stream().map(this::convertToUser).collect(Collectors.toList());
        return BaseResponse.success(users);
    }

    private User convertToUser(UserProfile profile) {
        return User.builder()
                .userId(profile.getUserId())
                .displayName(profile.getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .lastSeen(profile.getLastSeen())
                .isOnline(profile.isOnline())
                .privacySettings(profile.getPrivacySettings())
                .blockedUsers(profile.getBlockedUsers())
                .contacts(profile.getContacts())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
