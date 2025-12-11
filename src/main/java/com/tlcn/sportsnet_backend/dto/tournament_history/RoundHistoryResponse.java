package com.tlcn.sportsnet_backend.dto.tournament_history;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoundHistoryResponse {
    String id;

    Integer round;

    String opponentId;
    String opponentName;

    boolean won;

    List<Integer> scoreP1;
    List<Integer> scoreP2;
}
