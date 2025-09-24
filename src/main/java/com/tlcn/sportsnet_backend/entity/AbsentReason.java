package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.RequestStatusEnum;
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
@Table(name = "absent_reason")
public class AbsentReason {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false)
    ClubEventParticipant participation;


    @Column(nullable = false)
    @Lob
    String reason; // Lý do vắng mặt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RequestStatusEnum status = RequestStatusEnum.PENDING; // PENDING, APPROVED, REJECTED

    Instant createdAt;
    Instant reviewedAt;

    @PrePersist
    public void onCreate() {
        createdAt = Instant.now();
        status = RequestStatusEnum.PENDING;
    }


}
