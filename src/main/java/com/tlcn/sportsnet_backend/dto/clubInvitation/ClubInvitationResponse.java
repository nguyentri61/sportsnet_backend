package com.tlcn.sportsnet_backend.dto.clubInvitation;

import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubInvitationResponse {
    String id;
    String clubId;
    String clubName;
    String receiverId;
    String receiverName;
    String message;
    InvitationStatusEnum status;
    Instant sendAt;
    Instant respondedAt;
}
