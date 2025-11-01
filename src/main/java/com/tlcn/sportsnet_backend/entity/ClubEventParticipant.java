package com.tlcn.sportsnet_backend.entity;

import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ParticipantStatusEnum;
import com.tlcn.sportsnet_backend.util.SecurityUtil;
import com.tlcn.sportsnet_backend.util.SlugUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "club_event_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    boolean isClubMember;

    Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_event_id", nullable = false)
    ClubEvent clubEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    Account participant;

    @Enumerated(EnumType.STRING)
    ClubEventParticipantStatusEnum status;

    boolean deductedForAbsent = false; // đã trừ điểm hay chưa

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ClubEventCancellation> cancellations = new ArrayList<>();

    @PrePersist
    public void handleBeforeCreate(){
        joinedAt = Instant.now();
    }
}