package com.tlcn.sportsnet_backend.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingRequest {
    private String conversationId;
    private String senderId;
    private boolean typing; // true: đang gõ, false: dừng
}
