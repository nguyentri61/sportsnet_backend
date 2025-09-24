package com.tlcn.sportsnet_backend.entity;

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
@Table(name = "reputation_history")
public class ReputationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    Account account;
    @Column(name = "score_change", nullable = false)
    int change; // +10, -20, v.v.

    String reason; // Ví dụ: "Tham gia đầy đủ", "Vắng mặt không lý do", "Vắng mặt có lý do được duyệt"

    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
    }
}
