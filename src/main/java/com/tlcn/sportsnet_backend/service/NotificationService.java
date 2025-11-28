package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.NotificationMessage;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.NotificationTypeEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.error.UnauthorizedException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final AccountRepository accountRepository;
    private final ClubMemberRepository clubMemberRepository;
    public void sendToClub(String clubId, String title, String content, String link) {
        Instant now = Instant.now();
        List<ClubMember> clubMembers = clubMemberRepository.findByClubIdAndStatus(clubId, ClubMemberStatusEnum.APPROVED);
        Notification notification = Notification.builder()
                .clubId(clubId)
                .title(title)
                .content(content)
                .link(link)
                .createdAt(now)
                .type(NotificationTypeEnum.CLUB)
                .recipients(new ArrayList<>())
                .build();

        System.out.println(clubMembers.size());
        // Tạo danh sách người nhận
        clubMembers.stream()
                .map(ClubMember::getAccount)
                .forEach(acc -> {
                    NotificationRecipient recipient = NotificationRecipient.builder()
                            .notification(notification)
                            .account(acc)
                            .isRead(false)
                            .build();
                    notification.getRecipients().add(recipient);
                });

        notificationRepository.save(notification);

        // Gửi WS cho từng người
        NotificationMessage msg = new NotificationMessage(notification.getId(),title, content, link, now, false);
        for (NotificationRecipient r : notification.getRecipients()) {
            messagingTemplate.convertAndSend("/topic/account/" + r.getAccount().getId(), msg);
        }
    }

    public void sendToAccount(String email, String title, String content, String link) {
        Account account = accountRepository.findByEmail(email).orElseThrow(() ->  new InvalidDataException("Không tìm thấy email"));
        Instant now = Instant.now();
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .link(link)
                .createdAt(now)
                .type(NotificationTypeEnum.CLUB)
                .recipients(new ArrayList<>())
                .build();

        NotificationRecipient recipient = NotificationRecipient.builder()
                            .notification(notification)
                            .account(account)
                            .isRead(false)
                            .build();
        notification.getRecipients().add(recipient);

        notificationRepository.save(notification);

        // Gửi WS cho từng người
        NotificationMessage msg = new NotificationMessage(notification.getId(),title, content, link, now, false);
        for (NotificationRecipient r : notification.getRecipients()) {
            messagingTemplate.convertAndSend("/topic/account/" + r.getAccount().getId(), msg);
        }
    }
//    public void sendBroadcast(String title, String content, String link) {
//        NotificationMessage msg = new NotificationMessage(title, content, link, Instant.now());
//        messagingTemplate.convertAndSend("/topic/notifications", msg);
//    }

    public PagedResponse<NotificationMessage> getByAccount(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Notification> notifications = notificationRepository.findNotificationsByAccountId(account.getId(), pageable);

        List<NotificationMessage> content = notifications.getContent()
                .stream()
                .map(notification -> toNotificationMessage(notification, account))
                .toList();

        return new PagedResponse<>(
                content,
                notifications.getNumber(),
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages(),
                notifications.isLast()
        );
    }


    public void readUserNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));
        List<Notification> notifications = notificationRepository.findNotificationsByAccountId(account.getId());
        for (Notification notification : notifications) {
            readNotification(notification, account);
        }
    }
    public NotificationMessage toNotificationMessage(Notification notification, Account account) {
        NotificationRecipient recipient = notificationRecipientRepository.findByNotificationIdAndAccountId(notification.getId(), account.getId());
        return   NotificationMessage.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .link(notification.getLink())
                .timestamp(notification.getCreatedAt())
                .isRead(recipient.isRead())
                .build();
    }
    public void readNotification(Notification notification, Account account) {
        NotificationRecipient recipient = notificationRecipientRepository.findByNotificationIdAndAccountId(notification.getId(), account.getId());
        recipient.setRead(true);
        notificationRecipientRepository.save(recipient);
    }

}
