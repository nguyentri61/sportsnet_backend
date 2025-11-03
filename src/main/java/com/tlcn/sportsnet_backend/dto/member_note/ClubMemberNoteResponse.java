package com.tlcn.sportsnet_backend.dto.member_note;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubMemberNoteResponse {
    String id;
    String comment;
    Instant createdAt;
    Instant updatedAt;
    String createdBy;
    String updatedBy;
}
