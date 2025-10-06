package com.tlcn.sportsnet_backend.dto.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConversationResponse {
    String id;
    String name;
    String firstMessage;
    String avatarUrl;
    Instant lastMessageTime;
    Long unreadCount;
    boolean isGroupChat;
}
