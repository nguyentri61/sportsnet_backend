package com.tlcn.sportsnet_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.Instant;

@Entity
@Table(name = "club_event_cancellations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    ClubEventParticipant participant;

    @Lob
    String reason; // Lý do hủy muộn

    Boolean approved; // null: chưa duyệt, true: duyệt, false: từ chối

    Instant requestedAt;
    Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by") // người phê duyệt (chủ CLB)
    Account reviewedBy;

    @PrePersist
    public void handleBeforeCreate() {
        requestedAt = Instant.now();
    }
}
