package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Conversation;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    Account sender;

    @Lob
    @Column(columnDefinition = "TEXT")
    String content;

    Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    List<MessageStatus> statuses = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
