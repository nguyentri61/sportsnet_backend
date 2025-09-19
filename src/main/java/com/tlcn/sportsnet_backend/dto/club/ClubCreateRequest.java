package com.tlcn.sportsnet_backend.dto.club;

import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubCreateRequest {
    String name;
    String description;
    String logoUrl;
    String location;
    int maxMembers;
    double minLevel;
    double maxLevel;
    ClubVisibilityEnum visibility;
    Set<String> tags;
}
