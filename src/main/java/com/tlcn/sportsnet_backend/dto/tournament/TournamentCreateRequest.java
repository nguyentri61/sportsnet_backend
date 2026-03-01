package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.enums.TournamentParticipationTypeEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentCreateRequest {
    String name;
    String description;
    String location;
    String facilityId;
    String bannerUrl;
    String logoUrl;
    BigDecimal fee;
    String rules;
    LocalDateTime startDate;
    LocalDateTime endDate;
    LocalDateTime registrationStartDate;
    LocalDateTime registrationEndDate;
    
    // Loại hình tournament: cá nhân hoặc CLB
    TournamentParticipationTypeEnum participationType;

    // Danh sách các hạng mục thi đấu (category) trong giải này
    List<TournamentCategoryRequest> categories;
}
