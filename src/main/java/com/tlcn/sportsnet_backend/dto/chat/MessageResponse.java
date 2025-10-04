package com.tlcn.sportsnet_backend.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {
    private String id;
    private String content;
    private String senderName;
    private String senderAvatar;
    private boolean isReceived;
    private Instant createdAt;
}
