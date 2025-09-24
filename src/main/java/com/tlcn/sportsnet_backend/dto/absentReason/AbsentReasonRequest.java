package com.tlcn.sportsnet_backend.dto.absentReason;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AbsentReasonRequest {
    String idEvent;
    String reason; // Lý do vắng mặt
}
