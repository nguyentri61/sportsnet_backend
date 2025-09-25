package com.tlcn.sportsnet_backend.dto.club_event_participant;

import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventParticipantUpdate {
    ClubEventParticipantStatusEnum status;
}
