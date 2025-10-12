package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import jakarta.persistence.Column;
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
public class ClubEventAdminResponse {
    String id;
    String title;
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
    EventStatusEnum status;
    String slug;
    BigDecimal fee;
}
