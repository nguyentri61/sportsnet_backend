package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.TournamentFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentCategoryRequest {
    BadmintonCategoryEnum categoryType;

    double minLevel;   // 0.0 - 5.0
    double maxLevel;   // 0.0 - 5.0

    Integer maxParticipants;
    Double registrationFee;

    // ===== CLB Tournament Fields (nullable for individual tournaments) =====
    Double clubRegistrationFee; // Phí đăng ký cho CLB
    Integer minClubRosterSize; // Số lượng thành viên tối thiểu trong roster CLB
    Integer maxClubRosterSize; // Số lượng thành viên tối đa trong roster CLB
    String teamMatchFormat; // JSON string: {"singles": 3, "menDoubles": 2, "mixedDoubles": 1}
    // ===== End CLB Tournament Fields =====

    String description;

    String rules; // danh sách rule FE gửi dạng array

    String firstPrize;
    String secondPrize;
    String thirdPrize;

    TournamentFormat format;

}