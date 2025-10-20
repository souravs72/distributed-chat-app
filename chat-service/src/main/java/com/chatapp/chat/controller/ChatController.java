package com.chatapp.chat.controller;

import com.chatapp.chat.service.ChatService;
import com.chatapp.common.dto.BaseResponse;
import com.chatapp.common.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/messages")
    public ResponseEntity<BaseResponse<Message>> sendMessage(@RequestBody Message message) {
        BaseResponse<Message> response = chatService.sendMessage(message);
        return ResponseEntity.status(response.isSuccess() ? 201 : 400).body(response);
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<BaseResponse<List<Message>>> getMessages(
            @PathVariable String senderId,
            @PathVariable String recipientId,
            @RequestParam(defaultValue = "50") int limit) {
        BaseResponse<List<Message>> response = chatService.getMessages(senderId, recipientId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/{senderId}/{recipientId}/before")
    public ResponseEntity<BaseResponse<List<Message>>> getMessagesBeforeTimestamp(
            @PathVariable String senderId,
            @PathVariable String recipientId,
            @RequestParam LocalDateTime timestamp,
            @RequestParam(defaultValue = "50") int limit) {
        BaseResponse<List<Message>> response = chatService.getMessagesBeforeTimestamp(senderId, recipientId, timestamp, limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/messages/{messageId}/status")
    public ResponseEntity<BaseResponse<Message>> updateMessageStatus(
            @PathVariable String messageId,
            @RequestBody UpdateStatusRequest request) {
        BaseResponse<Message> response = chatService.updateMessageStatus(
                messageId, request.getSenderId(), request.getRecipientId(), request.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/unread/{recipientId}")
    public ResponseEntity<BaseResponse<List<Message>>> getUnreadMessages(@PathVariable String recipientId) {
        BaseResponse<List<Message>> response = chatService.getUnreadMessages(recipientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/messages/{messageId}/edit")
    public ResponseEntity<BaseResponse<Message>> editMessage(
            @PathVariable String messageId,
            @RequestBody EditMessageRequest request) {
        BaseResponse<Message> response = chatService.editMessage(
                messageId, request.getSenderId(), request.getRecipientId(), request.getNewContent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages/{senderId}/{recipientId}/history")
    public ResponseEntity<BaseResponse<List<Message>>> getMessageHistory(
            @PathVariable String senderId,
            @PathVariable String recipientId,
            @RequestParam LocalDateTime startRange,
            @RequestParam LocalDateTime endRange) {
        BaseResponse<List<Message>> response = chatService.getMessageHistory(senderId, recipientId, startRange, endRange);
        return ResponseEntity.ok(response);
    }

    // DTO classes
    public static class UpdateStatusRequest {
        private String senderId;
        private String recipientId;
        private Message.MessageStatus status;

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
        public Message.MessageStatus getStatus() { return status; }
        public void setStatus(Message.MessageStatus status) { this.status = status; }
    }

    public static class EditMessageRequest {
        private String senderId;
        private String recipientId;
        private String newContent;

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getRecipientId() { return recipientId; }
        public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
        public String getNewContent() { return newContent; }
        public void setNewContent(String newContent) { this.newContent = newContent; }
    }
}
