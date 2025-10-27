package com.tlcn.sportsnet_backend.dto.clubInvitation;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubInvitationRequest {
    String receiverId;
    String clubId;
    String message;

}
