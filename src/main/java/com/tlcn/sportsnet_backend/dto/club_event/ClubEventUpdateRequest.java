package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ParticipantRoleEnum;
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
public class ClubEventUpdateRequest {
    String id;
    String title;
    String description;
    String image;
    String location;
    String requirements;
    LocalDateTime startTime;
    LocalDateTime endTime;
    List<BadmintonCategoryEnum> categories;
    EventStatusEnum status;
    BigDecimal fee;
    LocalDateTime deadline;
    boolean openForOutside;
}
