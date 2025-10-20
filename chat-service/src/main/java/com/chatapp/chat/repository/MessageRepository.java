package com.chatapp.chat.repository;

import com.chatapp.chat.model.MessageEntity;
import com.chatapp.chat.model.MessageKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends CassandraRepository<MessageEntity, MessageKey> {
    
    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND timestamp >= ?1 AND timestamp <= ?2 ORDER BY timestamp DESC")
    List<MessageEntity> findByChatIdAndTimestampBetween(String chatId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT * FROM messages WHERE chat_id = ?0 ORDER BY timestamp DESC LIMIT ?1")
    List<MessageEntity> findLatestMessagesByChatId(String chatId, int limit);
    
    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND timestamp < ?1 ORDER BY timestamp DESC LIMIT ?2")
    List<MessageEntity> findMessagesBeforeTimestamp(String chatId, LocalDateTime timestamp, int limit);
    
    @Query("SELECT * FROM messages WHERE chat_id = ?0 AND message_id = ?1")
    Optional<MessageEntity> findByChatIdAndMessageId(String chatId, String messageId);
    
    @Query("SELECT * FROM messages WHERE sender_id = ?0 ORDER BY timestamp DESC LIMIT ?1")
    List<MessageEntity> findLatestMessagesBySenderId(String senderId, int limit);
    
    @Query("SELECT * FROM messages WHERE recipient_id = ?0 AND status = 'SENT' ORDER BY timestamp DESC LIMIT ?1")
    List<MessageEntity> findUnreadMessagesByRecipientId(String recipientId, int limit);
}
