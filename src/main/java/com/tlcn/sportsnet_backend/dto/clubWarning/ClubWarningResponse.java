package com.tlcn.sportsnet_backend.dto.clubWarning;

import com.tlcn.sportsnet_backend.enums.WarningStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubWarningResponse {
    String id;
    String reason;
    WarningStatus status;
    Instant createdAt;
    Instant updatedAt;

}
