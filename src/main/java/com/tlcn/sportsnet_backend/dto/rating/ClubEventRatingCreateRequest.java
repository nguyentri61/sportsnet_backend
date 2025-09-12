package com.tlcn.sportsnet_backend.dto.rating;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventRatingCreateRequest {
    private String eventClubId;
    private Integer rating;
    private String comment;
}
