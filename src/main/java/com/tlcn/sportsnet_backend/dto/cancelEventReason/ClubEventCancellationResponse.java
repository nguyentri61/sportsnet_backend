package com.tlcn.sportsnet_backend.dto.cancelEventReason;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventCancellationResponse {
    String cancellationId;
    String participantId;
    String accountSlug;
    String avatarUrl;
    String fullName;
    String email;
    String reason;
    Boolean approved; // null = chờ duyệt, true = duyệt, false = từ chối
    Boolean lateCancellation;
    Instant reviewedAt;
    String reviewedBy;
    Instant cancelDate;
}
