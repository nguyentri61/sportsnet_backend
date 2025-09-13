package com.tlcn.sportsnet_backend.dto.rating;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubEventRatingResponse {
    private String id;
    private String nameSender;
    private String eventName;
    private Integer rating;
    private String comment;
    private String avatarUrl;
    private Instant createdAt;
    private String replyComment;
    private Instant replyCreatedAt;
}
