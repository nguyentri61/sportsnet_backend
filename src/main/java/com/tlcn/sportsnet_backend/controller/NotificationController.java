package com.tlcn.sportsnet_backend.controller;


import com.tlcn.sportsnet_backend.entity.Notification;
import com.tlcn.sportsnet_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/user")
    public ResponseEntity<?> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getByAccount(page, size));
    }


    @PostMapping("/read")
    public ResponseEntity<?> readUserNotifications() {
        notificationService.readUserNotifications();
        return ResponseEntity.ok("Người dùng đã đọc thông báo");
    }
}

