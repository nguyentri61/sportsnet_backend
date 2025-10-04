package com.tlcn.sportsnet_backend.entity;
import com.tlcn.sportsnet_backend.enums.ChatRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "conversation_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"conversation_id", "account_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;

    @Enumerated(EnumType.STRING)
    ChatRole role; // MEMBER, ADMIN
}

