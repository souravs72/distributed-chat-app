package com.chatapp.chat.service;

import com.chatapp.chat.model.MessageEntity;
import com.chatapp.chat.model.MessageKey;
import com.chatapp.chat.repository.MessageRepository;
import com.chatapp.common.dto.BaseResponse;
import com.chatapp.common.event.ChatEvent;
import com.chatapp.common.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final MessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public BaseResponse<Message> sendMessage(Message message) {
        try {
            // Generate message ID if not provided
            if (message.getMessageId() == null) {
                message.setMessageId(UUID.randomUUID().toString());
            }

            // Set timestamps
            LocalDateTime now = LocalDateTime.now();
            if (message.getTimestamp() == null) {
                message.setTimestamp(now);
            }
            if (message.getCreatedAt() == null) {
                message.setCreatedAt(now);
            }
            if (message.getUpdatedAt() == null) {
                message.setUpdatedAt(now);
            }

            // Set initial status
            if (message.getStatus() == null) {
                message.setStatus(Message.MessageStatus.SENT);
            }

            // Generate chat ID
            String chatId = MessageKey.generateChatId(message.getSenderId(), message.getRecipientId());

            // Create message key
            MessageKey key = MessageKey.builder()
                    .chatId(chatId)
                    .timestamp(message.getTimestamp())
                    .messageId(message.getMessageId())
                    .senderId(message.getSenderId())
                    .recipientId(message.getRecipientId())
                    .build();

            // Create message entity
            MessageEntity entity = MessageEntity.fromMessage(message);
            entity.setKey(key);

            // Save message
            MessageEntity savedEntity = messageRepository.save(entity);
            Message savedMessage = savedEntity.toMessage();

            // Publish message sent event
            ChatEvent event = ChatEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(ChatEvent.EventType.MESSAGE_SENT)
                    .userId(message.getSenderId())
                    .targetUserId(message.getRecipientId())
                    .message(savedMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("message-events", message.getRecipientId(), event);

            return BaseResponse.success(savedMessage, "Message sent successfully");
        } catch (Exception e) {
            log.error("Error sending message from {} to {}", message.getSenderId(), message.getRecipientId(), e);
            return BaseResponse.error("Failed to send message", "MESSAGE_SEND_FAILED");
        }
    }

    public BaseResponse<List<Message>> getMessages(String senderId, String recipientId, int limit) {
        try {
            String chatId = MessageKey.generateChatId(senderId, recipientId);
            List<MessageEntity> entities = messageRepository.findLatestMessagesByChatId(chatId, limit);
            
            List<Message> messages = entities.stream()
                    .map(MessageEntity::toMessage)
                    .collect(Collectors.toList());

            return BaseResponse.success(messages);
        } catch (Exception e) {
            log.error("Error getting messages between {} and {}", senderId, recipientId, e);
            return BaseResponse.error("Failed to get messages", "MESSAGES_GET_FAILED");
        }
    }

    public BaseResponse<List<Message>> getMessagesBeforeTimestamp(String senderId, String recipientId, 
                                                                 LocalDateTime timestamp, int limit) {
        try {
            String chatId = MessageKey.generateChatId(senderId, recipientId);
            List<MessageEntity> entities = messageRepository.findMessagesBeforeTimestamp(chatId, timestamp, limit);
            
            List<Message> messages = entities.stream()
                    .map(MessageEntity::toMessage)
                    .collect(Collectors.toList());

            return BaseResponse.success(messages);
        } catch (Exception e) {
            log.error("Error getting messages before timestamp between {} and {}", senderId, recipientId, e);
            return BaseResponse.error("Failed to get messages", "MESSAGES_GET_FAILED");
        }
    }

    @Transactional
    public BaseResponse<Message> updateMessageStatus(String messageId, String senderId, String recipientId, 
                                                    Message.MessageStatus status) {
        try {
            String chatId = MessageKey.generateChatId(senderId, recipientId);
            Optional<MessageEntity> entityOpt = messageRepository.findByChatIdAndMessageId(chatId, messageId);
            
            if (entityOpt.isEmpty()) {
                return BaseResponse.error("Message not found", "MESSAGE_NOT_FOUND");
            }

            MessageEntity entity = entityOpt.get();
            entity.setStatus(status);
            entity.setUpdatedAt(LocalDateTime.now());

            MessageEntity savedEntity = messageRepository.save(entity);
            Message savedMessage = savedEntity.toMessage();

            // Publish status update event
            ChatEvent event = ChatEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(status == Message.MessageStatus.DELIVERED ? 
                            ChatEvent.EventType.MESSAGE_DELIVERED : ChatEvent.EventType.MESSAGE_SEEN)
                    .userId(recipientId)
                    .targetUserId(senderId)
                    .message(savedMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("message-events", senderId, event);

            return BaseResponse.success(savedMessage, "Message status updated successfully");
        } catch (Exception e) {
            log.error("Error updating message status for message {}", messageId, e);
            return BaseResponse.error("Failed to update message status", "MESSAGE_STATUS_UPDATE_FAILED");
        }
    }

    public BaseResponse<List<Message>> getUnreadMessages(String recipientId) {
        try {
            List<MessageEntity> entities = messageRepository.findUnreadMessagesByRecipientId(recipientId, 100);
            
            List<Message> messages = entities.stream()
                    .map(MessageEntity::toMessage)
                    .collect(Collectors.toList());

            return BaseResponse.success(messages);
        } catch (Exception e) {
            log.error("Error getting unread messages for user {}", recipientId, e);
            return BaseResponse.error("Failed to get unread messages", "UNREAD_MESSAGES_GET_FAILED");
        }
    }

    @Transactional
    public BaseResponse<Message> editMessage(String messageId, String senderId, String recipientId, String newContent) {
        try {
            String chatId = MessageKey.generateChatId(senderId, recipientId);
            Optional<MessageEntity> entityOpt = messageRepository.findByChatIdAndMessageId(chatId, messageId);
            
            if (entityOpt.isEmpty()) {
                return BaseResponse.error("Message not found", "MESSAGE_NOT_FOUND");
            }

            MessageEntity entity = entityOpt.get();
            entity.setContent(newContent);
            entity.setEdited(true);
            entity.setEditedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());

            MessageEntity savedEntity = messageRepository.save(entity);
            Message savedMessage = savedEntity.toMessage();

            // Publish message edited event
            ChatEvent event = ChatEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(ChatEvent.EventType.MESSAGE_EDITED)
                    .userId(senderId)
                    .targetUserId(recipientId)
                    .message(savedMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("message-events", recipientId, event);

            return BaseResponse.success(savedMessage, "Message edited successfully");
        } catch (Exception e) {
            log.error("Error editing message {}", messageId, e);
            return BaseResponse.error("Failed to edit message", "MESSAGE_EDIT_FAILED");
        }
    }

    public BaseResponse<List<Message>> getMessageHistory(String senderId, String recipientId, 
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String chatId = MessageKey.generateChatId(senderId, recipientId);
            List<MessageEntity> entities = messageRepository.findByChatIdAndTimestampBetween(chatId, startTime, endTime);
            
            List<Message> messages = entities.stream()
                    .map(MessageEntity::toMessage)
                    .collect(Collectors.toList());

            return BaseResponse.success(messages);
        } catch (Exception e) {
            log.error("Error getting message history between {} and {}", senderId, recipientId, e);
            return BaseResponse.error("Failed to get message history", "MESSAGE_HISTORY_GET_FAILED");
        }
    }
}
