package com.tlcn.sportsnet_backend.dto.clubWarning;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubWarningGetRequest {
    String accountId;
    String clubId;
}
