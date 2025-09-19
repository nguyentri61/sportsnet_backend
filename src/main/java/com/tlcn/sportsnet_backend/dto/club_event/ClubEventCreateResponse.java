package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventCreateResponse {
    String id;
    String slug;
    String title;
    String description;
    String image;
    String location;
    String requirements;
    LocalDateTime startTime;
    LocalDateTime endTime;
    double minLevel;
    double maxLevel;
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
