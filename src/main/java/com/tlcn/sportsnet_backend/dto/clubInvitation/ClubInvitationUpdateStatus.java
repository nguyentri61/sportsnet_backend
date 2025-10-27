package com.tlcn.sportsnet_backend.dto.clubInvitation;

import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubInvitationUpdateStatus {
    String id;
    InvitationStatusEnum status;
}
