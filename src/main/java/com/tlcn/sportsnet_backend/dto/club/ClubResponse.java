package com.tlcn.sportsnet_backend.dto.club;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
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
public class ClubResponse {
    String id;
    String slug;
    String name;
    String description;
    String logoUrl;
    String location;
    FacilityResponse facility;
    Integer memberCount;
    Integer maxMembers;
    Double minLevel;
    Double maxLevel;
    ClubVisibilityEnum visibility;
    Set<String> tags;
    ClubStatusEnum status;
    String ownerName;
    Instant createdAt;
}
