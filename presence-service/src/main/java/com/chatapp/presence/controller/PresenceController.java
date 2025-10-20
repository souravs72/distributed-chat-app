package com.chatapp.presence.controller;

import com.chatapp.common.dto.BaseResponse;
import com.chatapp.presence.model.Presence;
import com.chatapp.presence.model.TypingIndicator;
import com.chatapp.presence.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @PutMapping("/{userId}")
    public ResponseEntity<BaseResponse<Presence>> updatePresence(
            @PathVariable String userId,
            @RequestBody UpdatePresenceRequest request) {
        BaseResponse<Presence> response = presenceService.updatePresence(
                userId, request.isOnline(), request.getDeviceId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<Presence>> getPresence(@PathVariable String userId) {
        BaseResponse<Presence> response = presenceService.getPresence(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<BaseResponse<List<Presence>>> getPresences(@RequestBody List<String> userIds) {
        BaseResponse<List<Presence>> response = presenceService.getPresences(userIds);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/typing/{userId}/{targetUserId}")
    public ResponseEntity<BaseResponse<String>> setTypingIndicator(
            @PathVariable String userId,
            @PathVariable String targetUserId,
            @RequestBody TypingRequest request) {
        BaseResponse<String> response = presenceService.setTypingIndicator(
                userId, targetUserId, request.isTyping());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/typing/{userId}")
    public ResponseEntity<BaseResponse<List<TypingIndicator>>> getTypingIndicators(@PathVariable String userId) {
        BaseResponse<List<TypingIndicator>> response = presenceService.getTypingIndicators(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/online")
    public ResponseEntity<BaseResponse<List<String>>> getOnlineUsers() {
        BaseResponse<List<String>> response = presenceService.getOnlineUsers();
        return ResponseEntity.ok(response);
    }

    // DTO classes
    public static class UpdatePresenceRequest {
        private boolean online;
        private String deviceId;

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    }

    public static class TypingRequest {
        private boolean typing;

        public boolean isTyping() { return typing; }
        public void setTyping(boolean typing) { this.typing = typing; }
    }
}
