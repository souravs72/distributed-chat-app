package com.chatapp.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageKey {
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String chatId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 0)
    private LocalDateTime timestamp;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 1)
    private String messageId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
    private String senderId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3)
    private String recipientId;

    public static String generateChatId(String userId1, String userId2) {
        // Generate consistent chat ID by sorting user IDs
        String[] users = {userId1, userId2};
        java.util.Arrays.sort(users);
        return users[0] + "_" + users[1];
    }
}
