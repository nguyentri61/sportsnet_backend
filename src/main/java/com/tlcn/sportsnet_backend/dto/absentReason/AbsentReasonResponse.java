package com.tlcn.sportsnet_backend.dto.absentReason;

import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.enums.RequestStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AbsentReasonResponse {
    String id;
    String reason; // Lý do vắng mặt
    RequestStatusEnum status; // PENDING, APPROVED, REJECTED
    Instant createdAt;
    Instant reviewedAt;
}
