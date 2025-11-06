package com.tlcn.sportsnet_backend.dto.auth;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyRequest {
    String email;
    String otp;
}
