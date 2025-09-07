package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    private String title;
    private String content;
    private String link;

    private boolean isRead = false;
    private Instant createdAt ;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account account; // null nếu broadcast hoặc nhóm

    private String clubId;  // null nếu không thuộc CLB
    private String eventId; // null nếu không liên quan event

    private NotificationTypeEnum type; // optional: BROADCAST, CLUB, EVENT, SYSTEM
}
