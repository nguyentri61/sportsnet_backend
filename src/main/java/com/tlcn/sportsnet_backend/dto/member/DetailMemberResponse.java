package com.tlcn.sportsnet_backend.dto.member;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailMemberResponse {
    String id;
    String email;
    String fullName;
    LocalDate birthDate;
    String gender;
    String address;
    String bio;
    String avatarUrl;
    String phone;
    Instant createdAt;
    String note;
}
