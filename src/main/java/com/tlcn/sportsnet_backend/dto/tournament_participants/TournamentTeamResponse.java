package com.tlcn.sportsnet_backend.dto.tournament_participants;

import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentTeamResponse {
    String id;
    String teamName;
    String player1FullName;
    String player1Slug;
    String player1AvatarUrl;
    String player1Email;
    String player1Gender;
    String player2FullName;
    String player2Slug;
    String player2AvatarUrl;
    String player2Email;
    String player2Gender;
    TournamentParticipantEnum status;
    Instant createdAt;
}
