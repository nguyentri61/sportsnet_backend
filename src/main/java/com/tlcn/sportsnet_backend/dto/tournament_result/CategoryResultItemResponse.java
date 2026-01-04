package com.tlcn.sportsnet_backend.dto.tournament_result;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResultItemResponse {
    Integer ranking;
    String prize;

    String participantId;   // SINGLE
    String participantName;

    String teamId;          // DOUBLE
    String teamName;
}
