package com.tlcn.sportsnet_backend.dto.post;

import com.tlcn.sportsnet_backend.enums.MediaTypeEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MediaResponse {
    String url;
    MediaTypeEnum type;
}
