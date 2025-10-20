package com.chatapp.user.controller;

import com.chatapp.common.dto.BaseResponse;
import com.chatapp.common.model.User;
import com.chatapp.user.model.UserProfile;
import com.chatapp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/profile")
    public ResponseEntity<BaseResponse<UserProfile>> createUserProfile(
            @RequestBody CreateUserProfileRequest request) {
        BaseResponse<UserProfile> response = userService.createUserProfile(
                request.getUserId(), request.getDisplayName());
        return ResponseEntity.status(response.isSuccess() ? 201 : 400).body(response);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<BaseResponse<UserProfile>> getUserProfile(@PathVariable String userId) {
        BaseResponse<UserProfile> response = userService.getUserProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<BaseResponse<UserProfile>> updateUserProfile(
            @PathVariable String userId, @RequestBody UserProfile updatedProfile) {
        BaseResponse<UserProfile> response = userService.updateUserProfile(userId, updatedProfile);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{userId}/presence")
    public ResponseEntity<BaseResponse<String>> updatePresence(
            @PathVariable String userId, @RequestBody UpdatePresenceRequest request) {
        BaseResponse<String> response = userService.updatePresence(userId, request.isOnline());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/{userId}/contacts/{contactId}")
    public ResponseEntity<BaseResponse<String>> addContact(
            @PathVariable String userId, @PathVariable String contactId) {
        BaseResponse<String> response = userService.addContact(userId, contactId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile/{userId}/contacts/{contactId}")
    public ResponseEntity<BaseResponse<String>> removeContact(
            @PathVariable String userId, @PathVariable String contactId) {
        BaseResponse<String> response = userService.removeContact(userId, contactId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile/{userId}/block/{blockedUserId}")
    public ResponseEntity<BaseResponse<String>> blockUser(
            @PathVariable String userId, @PathVariable String blockedUserId) {
        BaseResponse<String> response = userService.blockUser(userId, blockedUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile/{userId}/block/{blockedUserId}")
    public ResponseEntity<BaseResponse<String>> unblockUser(
            @PathVariable String userId, @PathVariable String blockedUserId) {
        BaseResponse<String> response = userService.unblockUser(userId, blockedUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{userId}/contacts")
    public ResponseEntity<BaseResponse<List<UserProfile>>> getContacts(@PathVariable String userId) {
        BaseResponse<List<UserProfile>> response = userService.getContacts(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<UserProfile>>> searchUsers(@RequestParam String query) {
        BaseResponse<List<UserProfile>> response = userService.searchUsers(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/convert")
    public ResponseEntity<BaseResponse<List<User>>> convertToUserList(@RequestBody List<String> userIds) {
        BaseResponse<List<User>> response = userService.convertToUserList(userIds);
        return ResponseEntity.ok(response);
    }

    // DTO classes
    public static class CreateUserProfileRequest {
        private String userId;
        private String displayName;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    public static class UpdatePresenceRequest {
        private boolean online;

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
}
