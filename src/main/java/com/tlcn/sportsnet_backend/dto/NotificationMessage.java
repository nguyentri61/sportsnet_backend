package com.tlcn.sportsnet_backend.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationMessage {
    private String title;
    private String content;
    private String link;      // URL hoặc route FE
    private Instant timestamp;
}
