package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentCategoryRequest {
    private BadmintonCategoryEnum categoryType;

    private double minLevel;   // 0.0 - 5.0
    private double maxLevel;   // 0.0 - 5.0

    private Integer maxParticipants;
}