package com.tlcn.sportsnet_backend.dto.facility;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FacilityResponse {
    String id;
    String name;
    String address;
    String district;
    String city;
    String location;
    Double latitude;
    Double longitude;
    String image;
}
