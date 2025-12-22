package com.tlcn.sportsnet_backend.dto.tournament_result;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentResultResponse {
    String tournamentId;
    String tournamentName;

    List<CategoryResultResponse> categories;
}
