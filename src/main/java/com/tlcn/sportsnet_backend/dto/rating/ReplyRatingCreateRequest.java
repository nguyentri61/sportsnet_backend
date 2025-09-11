package com.tlcn.sportsnet_backend.dto.rating;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReplyRatingCreateRequest {
    private String ratingId;
    private String replyComment;
}
