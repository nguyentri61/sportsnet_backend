package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Notification;
import com.tlcn.sportsnet_backend.entity.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, String> {
    NotificationRecipient findByNotificationIdAndAccountId(String notificationId, String recipientId);
}
