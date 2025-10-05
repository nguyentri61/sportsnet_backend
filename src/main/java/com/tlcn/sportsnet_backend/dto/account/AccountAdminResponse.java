package com.tlcn.sportsnet_backend.dto.account;


import com.tlcn.sportsnet_backend.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountAdminResponse {
    String id;
    String email;
    String fullName;
    LocalDate birthDate;
    String gender;
    String address;
    String phone;
    boolean enabled;
    Instant createdAt;
    int reputationScore;
    int totalParticipatedEvents;
    List<OwnerClub> ownerClubs;
    String slug;
    String role;
    private double overallScore;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class OwnerClub{
        String clubName;
        String slug;
        String urlLogo;

    }
}
