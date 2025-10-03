package com.tlcn.sportsnet_backend.dto.chat;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingEvent {
    private String conversationId;
    private String senderId;
    private boolean typing; // true: đang gõ, false: dừng
}