package com.tlcn.sportsnet_backend.dto.bracket;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentMatchResponse {
    String matchId;
    Integer round;
    Integer matchIndex;
    String player1Id;
    String player2Id;
    String player1Name;
    String player2Name;
    List<Integer> setScoreP1;
    List<Integer> setScoreP2;
    String winnerId;
    String winnerName;
    String status;
}
