package com.tlcn.sportsnet_backend.controller;


import com.tlcn.sportsnet_backend.entity.Notification;
import com.tlcn.sportsnet_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/user")
    public List<Notification> getUserNotifications() {
        return notificationService.getByAccount();
    }
}

