package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    String description;
    String image;
    String location;
    LocalDate date;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int totalMember;
    List<BadmintonCategoryEnum> categories;
    EventStatusEnum status;
    String clubId;

    BigDecimal fee;
    LocalDateTime deadline;
    boolean openForOutside;
    int maxClubMembers;
    int maxOutsideMembers;

    Instant createdAt;
    String createdBy;
}
