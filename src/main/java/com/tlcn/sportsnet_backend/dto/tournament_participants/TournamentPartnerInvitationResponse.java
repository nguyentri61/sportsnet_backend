package com.tlcn.sportsnet_backend.dto.tournament_participants;

import com.tlcn.sportsnet_backend.dto.account.AccountFriend;
import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentPartnerInvitationResponse {
    String id;
    AccountFriend inviter;
    AccountFriend invitee;
    InvitationStatusEnum status;
    String message;
    boolean isSend;
    Instant createdAt;
}
