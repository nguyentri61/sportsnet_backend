package com.tlcn.sportsnet_backend.dto.post;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostUpdateRequest {
    String id;
    String content;
    List<String> newFileNames;
    List<String> keepFileNames;
}
