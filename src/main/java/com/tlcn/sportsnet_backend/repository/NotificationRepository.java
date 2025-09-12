package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Notification;
import com.tlcn.sportsnet_backend.entity.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // Trả về trực tiếp Notification theo accountId
    @Query("SELECT nr.notification FROM NotificationRecipient nr WHERE nr.account.id = :accountId")
    List<Notification> findNotificationsByAccountId(@Param("accountId") String accountId);
    @Query("SELECT n FROM NotificationRecipient nr " +
            "JOIN nr.notification n " +
            "WHERE nr.account.id = :accountId " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findNotificationsByAccountId(@Param("accountId") String accountId, Pageable pageable);

}