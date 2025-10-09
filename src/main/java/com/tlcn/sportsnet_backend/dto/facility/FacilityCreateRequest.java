package com.tlcn.sportsnet_backend.dto.facility;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FacilityCreateRequest {
    String name;
    String address;
    String district;
    String city;
    Double latitude;
    Double longitude;
    String image;
}
