package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Notification;
import com.tlcn.sportsnet_backend.entity.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, String> {
    NotificationRecipient findByNotificationIdAndAccountId(String notificationId, String recipientId);

    @Query("""
SELECT nr FROM NotificationRecipient nr
WHERE nr.account.id = :accountId
AND nr.notification.id IN :notificationIds
""")
    List<NotificationRecipient> findByAccountIdAndNotificationIds(
            @Param("accountId") Long accountId,
            @Param("notificationIds") List<Long> notificationIds
    );
}
