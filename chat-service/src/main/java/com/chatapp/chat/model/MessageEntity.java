package com.chatapp.chat.model;

import com.chatapp.common.model.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Table("messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
    @PrimaryKey
    private MessageKey key;
    private String content;
    private Message.MessageType messageType;
    private Message.MessageStatus status;
    private Map<String, Object> metadata;
    private String replyToMessageId;
    private boolean isEdited;
    private LocalDateTime editedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Message toMessage() {
        return Message.builder()
                .messageId(key.getMessageId())
                .senderId(key.getSenderId())
                .recipientId(key.getRecipientId())
                .content(content)
                .messageType(messageType)
                .timestamp(key.getTimestamp())
                .status(status)
                .metadata(metadata)
                .replyToMessageId(replyToMessageId)
                .isEdited(isEdited)
                .editedAt(editedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    public static MessageEntity fromMessage(Message message) {
        MessageKey key = MessageKey.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .timestamp(message.getTimestamp())
                .build();

        return MessageEntity.builder()
                .key(key)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .metadata(message.getMetadata())
                .replyToMessageId(message.getReplyToMessageId())
                .isEdited(message.isEdited())
                .editedAt(message.getEditedAt())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}
