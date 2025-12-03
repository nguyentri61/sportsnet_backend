package com.tlcn.sportsnet_backend.dto.bracket;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetScore {
    Integer p1;
    Integer p2;
}
