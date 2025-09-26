package com.tlcn.sportsnet_backend.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostTagResponse {
    String id;
    String fullName;
    String slug;
}
