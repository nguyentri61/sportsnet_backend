package com.tlcn.sportsnet_backend.dto.chatbot;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatbotRequest {
    String message;
}
