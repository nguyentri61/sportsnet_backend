package com.tlcn.sportsnet_backend.dto.club;

import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
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
public class MyClubResponse {
    String id;
    String name;
    String description;
    String logoUrl;
    String location;
    Integer memberCount;
    Integer maxMembers;
    ClubVisibilityEnum visibility;
    Set<String> tags;
    ClubMemberStatusEnum memberStatus;
    boolean active;
    boolean isOwner;
    Instant dateJoined;
    String ownerName;
    Instant createdAt;
}
