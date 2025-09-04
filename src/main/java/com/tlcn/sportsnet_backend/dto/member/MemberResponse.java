package com.tlcn.sportsnet_backend.dto.member;

import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemberResponse {

    String id;
    String name;
    String avatar;

    @Enumerated(EnumType.STRING)
    ClubMemberRoleEnum role; // MEMBER, ADMIN
    @Enumerated(EnumType.STRING)
    ClubMemberStatusEnum status; // ACTIVE, BANNED, PENDING_APPROVAL
    Instant joinedAt;

}
