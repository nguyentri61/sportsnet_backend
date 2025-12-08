package com.tlcn.sportsnet_backend.dto.tournament_history;

import com.tlcn.sportsnet_backend.entity.RoundHistory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlayerTournamentHistoryResponse {
    String historyId;

    TournamentInfoResponse tournament;

    CategoryInfoResponse category;

    boolean isDouble;
    String teamId;

    Integer finalRanking;
    String prize;

    List<RoundHistory> rounds;

    Instant createdAt;
}
