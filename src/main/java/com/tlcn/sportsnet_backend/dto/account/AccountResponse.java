package com.tlcn.sportsnet_backend.dto.account;

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
public class AccountResponse {
    String id;
    String email;
    String fullName;
    LocalDate birthDate;
    String gender;
    String address;
    String bio;
    String avatarUrl;
    String phone;
    boolean enabled;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
    int reputationScore;
    int totalParticipatedEvents;
    List<OwnerClub> ownerClubs;

    @AllArgsConstructor
    public static class OwnerClub{
        String clubName;
        String slug;

    }

}



