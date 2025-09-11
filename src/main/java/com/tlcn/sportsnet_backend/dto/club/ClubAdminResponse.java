package com.tlcn.sportsnet_backend.dto.club;

import com.tlcn.sportsnet_backend.enums.ClubStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubAdminResponse {
    String id;
    String slug;
    String name;
    String ownerName;
    String email;
    Integer memberCount;
    Integer maxMembers;
    ClubStatusEnum status;
    Instant createdAt;
}