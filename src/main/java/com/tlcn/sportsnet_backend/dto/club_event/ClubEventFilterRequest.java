package com.tlcn.sportsnet_backend.dto.club_event;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventFilterRequest {
    List<String> levels;
    List<BadmintonCategoryEnum> categories;
    String participantSize;
    Double minRating;
    List<String> facilityNames;
    List<EventStatusEnum> statuses;
}
