package com.tlcn.sportsnet_backend.dto.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountUpdatePassword {
    String newPassword;
    String password;
    String confirmPassword;
}
