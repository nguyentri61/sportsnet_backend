package com.tlcn.sportsnet_backend.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tlcn.sportsnet_backend.enums.StatusScheduleEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleResponse {
    String id;
    String name;
    // Thời gian bắt đầu của lịch
    LocalDateTime startTime;

    // Thời gian kết thúc của lịch
    LocalDateTime endTime;

    StatusScheduleEnum status;

    Instant createdAt;
    String slug;
}
