package com.tlcn.sportsnet_backend.dto.bracket;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BracketRoundResponse {
    Integer round;
    List<TournamentMatchResponse> matches;
}
