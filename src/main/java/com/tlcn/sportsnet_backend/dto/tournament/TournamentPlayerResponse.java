package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentPlayerResponse {
    private String id;
    private BadmintonCategoryEnum category;
    List<PlayerResponse> players;
    List<TeamResponse> teams;
}

