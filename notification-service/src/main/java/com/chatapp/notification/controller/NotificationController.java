package com.chatapp.notification.controller;

import com.chatapp.common.dto.BaseResponse;
import com.chatapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/test/{userId}")
    public ResponseEntity<BaseResponse<String>> sendTestNotification(
            @PathVariable String userId,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification") String body) {
        BaseResponse<String> response = notificationService.sendTestNotification(userId, title, body);
        return ResponseEntity.ok(response);
    }
}
