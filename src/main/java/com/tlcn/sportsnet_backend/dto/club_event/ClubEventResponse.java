package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.entity.Facility;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ParticipantRoleEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    FacilityResponse facility;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int totalMember;
    int joinedMember;
    String nameClub;
    boolean openForOutside;
    double minLevel;
    double maxLevel;
    ParticipantRoleEnum participantRole;
    int maxOutsideMembers;
    int joinedOpenMembers;
    BigDecimal fee;
    Set<BadmintonCategoryEnum> categories;
    EventStatusEnum status;
}
