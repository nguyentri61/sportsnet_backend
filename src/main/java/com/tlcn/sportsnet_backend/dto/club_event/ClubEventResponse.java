package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ParticipantRoleEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventResponse {
    String id;
    String title;
    String slug;
    String image;
    String location;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int totalMember;
    int joinedMember;
    String nameClub;
    boolean openForOutside;
    ParticipantRoleEnum participantRole;
    int maxOutsideMembers;
    int joinedOpenMembers;
    BigDecimal fee;
    List<BadmintonCategoryEnum> categories;
    EventStatusEnum status;
}
