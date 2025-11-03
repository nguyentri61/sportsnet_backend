package com.tlcn.sportsnet_backend.dto.member_note;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClubMemberNoteRequest {
    String clubId;
    String accountId;
    String comment;
}
