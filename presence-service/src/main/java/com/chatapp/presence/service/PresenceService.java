package com.chatapp.presence.service;

import com.chatapp.common.dto.BaseResponse;
import com.chatapp.common.event.ChatEvent;
import com.chatapp.presence.model.Presence;
import com.chatapp.presence.model.TypingIndicator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PRESENCE_KEY_PREFIX = "presence:";
    private static final String TYPING_KEY_PREFIX = "typing:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user_sessions:";
    private static final int PRESENCE_TTL = 300; // 5 minutes
    private static final int TYPING_TTL = 10; // 10 seconds

    public BaseResponse<Presence> updatePresence(String userId, boolean isOnline, String deviceId) {
        try {
            Presence presence = Presence.builder()
                    .userId(userId)
                    .isOnline(isOnline)
                    .lastSeen(LocalDateTime.now())
                    .status(isOnline ? Presence.Status.ONLINE.name() : Presence.Status.OFFLINE.name())
                    .deviceId(deviceId)
                    .updatedAt(LocalDateTime.now())
                    .build();

            String key = PRESENCE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(key, presence, PRESENCE_TTL, TimeUnit.SECONDS);

            // Store user session for multi-device support
            if (isOnline) {
                String sessionKey = USER_SESSIONS_KEY_PREFIX + userId + ":" + deviceId;
                redisTemplate.opsForValue().set(sessionKey, deviceId, PRESENCE_TTL, TimeUnit.SECONDS);
            }

            // Publish presence change event
            ChatEvent event = ChatEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(isOnline ? ChatEvent.EventType.USER_ONLINE : ChatEvent.EventType.USER_OFFLINE)
                    .userId(userId)
                    .payload(presence)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("presence-events", userId, event);

            return BaseResponse.success(presence, "Presence updated successfully");
        } catch (Exception e) {
            log.error("Error updating presence for user: {}", userId, e);
            return BaseResponse.error("Failed to update presence", "PRESENCE_UPDATE_FAILED");
        }
    }

    public BaseResponse<Presence> getPresence(String userId) {
        try {
            String key = PRESENCE_KEY_PREFIX + userId;
            Presence presence = (Presence) redisTemplate.opsForValue().get(key);
            
            if (presence == null) {
                // Return offline presence if not found
                presence = Presence.builder()
                        .userId(userId)
                        .isOnline(false)
                        .lastSeen(LocalDateTime.now())
                        .status(Presence.Status.OFFLINE.name())
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            return BaseResponse.success(presence);
        } catch (Exception e) {
            log.error("Error getting presence for user: {}", userId, e);
            return BaseResponse.error("Failed to get presence", "PRESENCE_GET_FAILED");
        }
    }

    public BaseResponse<List<Presence>> getPresences(List<String> userIds) {
        try {
            List<String> keys = userIds.stream()
                    .map(userId -> PRESENCE_KEY_PREFIX + userId)
                    .toList();

            List<Object> presences = redisTemplate.opsForValue().multiGet(keys);
            List<Presence> result = new ArrayList<>();

            for (int i = 0; i < userIds.size(); i++) {
                String userId = userIds.get(i);
                Presence presence = (Presence) presences.get(i);
                
                if (presence == null) {
                    presence = Presence.builder()
                            .userId(userId)
                            .isOnline(false)
                            .lastSeen(LocalDateTime.now())
                            .status(Presence.Status.OFFLINE.name())
                            .updatedAt(LocalDateTime.now())
                            .build();
                }
                result.add(presence);
            }

            return BaseResponse.success(result);
        } catch (Exception e) {
            log.error("Error getting presences for users: {}", userIds, e);
            return BaseResponse.error("Failed to get presences", "PRESENCES_GET_FAILED");
        }
    }

    public BaseResponse<String> setTypingIndicator(String userId, String targetUserId, boolean isTyping) {
        try {
            TypingIndicator indicator = TypingIndicator.builder()
                    .userId(userId)
                    .targetUserId(targetUserId)
                    .isTyping(isTyping)
                    .timestamp(LocalDateTime.now())
                    .chatId(generateChatId(userId, targetUserId))
                    .build();

            String key = TYPING_KEY_PREFIX + generateChatId(userId, targetUserId) + ":" + userId;
            
            if (isTyping) {
                redisTemplate.opsForValue().set(key, indicator, TYPING_TTL, TimeUnit.SECONDS);
            } else {
                redisTemplate.delete(key);
            }

            // Publish typing event
            ChatEvent event = ChatEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(isTyping ? ChatEvent.EventType.USER_TYPING : ChatEvent.EventType.USER_STOPPED_TYPING)
                    .userId(userId)
                    .targetUserId(targetUserId)
                    .payload(indicator)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("typing-events", targetUserId, event);

            return BaseResponse.success("Typing indicator updated successfully");
        } catch (Exception e) {
            log.error("Error setting typing indicator for user: {} to user: {}", userId, targetUserId, e);
            return BaseResponse.error("Failed to update typing indicator", "TYPING_UPDATE_FAILED");
        }
    }

    public BaseResponse<List<TypingIndicator>> getTypingIndicators(String userId) {
        try {
            String pattern = TYPING_KEY_PREFIX + "*:" + userId;
            Set<String> keys = redisTemplate.keys(pattern);
            
            List<TypingIndicator> indicators = new ArrayList<>();
            if (keys != null && !keys.isEmpty()) {
                List<Object> values = redisTemplate.opsForValue().multiGet(keys);
                for (Object value : values) {
                    if (value instanceof TypingIndicator) {
                        indicators.add((TypingIndicator) value);
                    }
                }
            }

            return BaseResponse.success(indicators);
        } catch (Exception e) {
            log.error("Error getting typing indicators for user: {}", userId, e);
            return BaseResponse.error("Failed to get typing indicators", "TYPING_GET_FAILED");
        }
    }

    public BaseResponse<List<String>> getOnlineUsers() {
        try {
            String pattern = PRESENCE_KEY_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            List<String> onlineUsers = new ArrayList<>();
            if (keys != null && !keys.isEmpty()) {
                List<Object> presences = redisTemplate.opsForValue().multiGet(keys);
                for (Object presence : presences) {
                    if (presence instanceof Presence && ((Presence) presence).isOnline()) {
                        onlineUsers.add(((Presence) presence).getUserId());
                    }
                }
            }

            return BaseResponse.success(onlineUsers);
        } catch (Exception e) {
            log.error("Error getting online users", e);
            return BaseResponse.error("Failed to get online users", "ONLINE_USERS_GET_FAILED");
        }
    }

    private String generateChatId(String userId1, String userId2) {
        // Generate consistent chat ID by sorting user IDs
        String[] users = {userId1, userId2};
        Arrays.sort(users);
        return users[0] + "_" + users[1];
    }
}
