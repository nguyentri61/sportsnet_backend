package com.tlcn.sportsnet_backend.dto.club_event_participant;

import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventParticipantResponse {
    String id;
    boolean isClubMember;
    Instant joinedAt;
    String email;
    String fullName;
    String gender;
    String avatarUrl;
    ClubEventParticipantStatusEnum status;
    Integer  experience;
    Integer  stamina;
    Integer  tactics;
    Double  averageTechnicalScore;
    Double  overallScore;
    String skillLevel;
    String slug;
    int reputationScore;
    int totalParticipatedEvents;
}
