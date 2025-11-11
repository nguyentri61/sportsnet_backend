package com.tlcn.sportsnet_backend.dto.tournament;


import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentCategoryResponse {
    private String id;
    private BadmintonCategoryEnum category;
    private Integer maxParticipants;
    private int currentParticipantCount;
    private TournamentParticipantEnum participantStatus;
}
