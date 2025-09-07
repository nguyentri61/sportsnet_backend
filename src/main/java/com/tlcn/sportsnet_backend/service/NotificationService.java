package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.NotificationMessage;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Notification;
import com.tlcn.sportsnet_backend.error.UnauthorizedException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;

    public void sendToClub(String clubId, String title, String content, String link) {
        Instant now = Instant.now();
        Notification notification = Notification.builder()
                .clubId(clubId)
                .title(title)
                .content(content)
                .link(link)
                .createdAt(now)
                .build();
        notificationRepository.save(notification);
        NotificationMessage msg = new NotificationMessage(title, content, link, now);
        messagingTemplate.convertAndSend("/topic/club/" + clubId, msg);
    }

    public void sendBroadcast(String title, String content, String link) {
        NotificationMessage msg = new NotificationMessage(title, content, link, Instant.now());
        messagingTemplate.convertAndSend("/topic/notifications", msg);
    }

    public List<Notification> getByAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));
        return notificationRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    public NotificationMessage toNotificationMessage(Notification notification) {
        return   NotificationMessage.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .link(notification.getLink())
                .timestamp(notification.getCreatedAt())
                .build();
    }
}
