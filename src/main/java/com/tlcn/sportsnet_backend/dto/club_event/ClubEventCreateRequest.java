package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventCreateRequest {
    String title;
    String description;
    String image;
    String location;
    LocalDate date;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int totalMember;
    List<BadmintonCategoryEnum> type;

    BigDecimal fee;
    LocalDateTime deadline;
    boolean openForOutside;
    int maxClubMembers;
    int maxOutsideMembers;

    String clubId;
}
