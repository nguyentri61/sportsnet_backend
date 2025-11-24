package com.tlcn.sportsnet_backend.dto.tournament_participants;

import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentPartnerInvitationUpdate {
    String id;
    InvitationStatusEnum status;
}
