package com.tlcn.sportsnet_backend.dto.club;

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
public class ClubResponse {
    String id;
    String name;
    String description;
    String logoUrl;
    String location;
    Integer maxMembers;
    ClubVisibilityEnum visibility;
    Set<String> tags;
    boolean active;
    String ownerName;
    Instant createdAt;
}
