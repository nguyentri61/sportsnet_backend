package com.tlcn.sportsnet_backend.dto.friend;

import com.tlcn.sportsnet_backend.enums.FriendStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendResponse {
    String id;
    FriendStatusEnum status;
    UserSummary requester;
    UserSummary receiver;
    Instant createdAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserSummary {
        String id;
        String email;
        String fullName;
    }
}
