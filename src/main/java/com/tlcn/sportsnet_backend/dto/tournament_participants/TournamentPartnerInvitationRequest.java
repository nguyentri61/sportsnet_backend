package com.tlcn.sportsnet_backend.dto.tournament_participants;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentPartnerInvitationRequest {
    String categoryId;
    String inviteeId;
    String message;
}
