package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentCategoryDetailResponse {
    String id;
    String tournamentName;
    FacilityResponse facility;
    LocalDateTime startDate;
    LocalDateTime endDate;
    BadmintonCategoryEnum category;
    Double minLevel;
    Double maxLevel;
    Integer maxParticipants;
    int currentParticipantCount;
    Double registrationFee;
    String description;
    List<String> rules;
    String firstPrize;
    String secondPrize;
    String thirdPrize;
    String format;
    LocalDateTime registrationDeadline;
    boolean admin;
    boolean isDouble;
    TournamentParticipantEnum participantStatus;
}


