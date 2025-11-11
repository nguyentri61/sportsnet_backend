package com.tlcn.sportsnet_backend.dto.tournament_participants;

import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentParticipantResponse {
    String id;
    String fullName;
    String slug;
    String avatarUrl;
    String email;
    TournamentParticipantEnum status;
}
