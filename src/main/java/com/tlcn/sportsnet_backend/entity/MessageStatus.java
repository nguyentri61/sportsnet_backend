package com.tlcn.sportsnet_backend.entity;
import com.tlcn.sportsnet_backend.enums.ChatRole;
import com.tlcn.sportsnet_backend.enums.MessageSeenStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "message_status",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"message_id", "account_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    private MessageSeenStatus status = MessageSeenStatus.UNSEEN; // SEEN, UNSEEN

    private Instant seenAt;
}
