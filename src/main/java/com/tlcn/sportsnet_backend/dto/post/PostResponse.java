package com.tlcn.sportsnet_backend.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String content;
    String authorName;
    String authorAvatar;
    Instant createdAt;
    List<MediaResponse> mediaList;
    int likeCount;
    int commentCount;
    String userId;
    String currentUserId;
}
