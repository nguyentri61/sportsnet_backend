package com.tlcn.sportsnet_backend.dto.tournament_history;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentInfoResponse {
    String tournamentId;
    String name;
    String location;
    String logoUrl;
    String bannerUrl;
    String slug;
    LocalDateTime startDate;
    LocalDateTime endDate;
}
