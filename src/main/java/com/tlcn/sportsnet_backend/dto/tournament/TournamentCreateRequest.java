package com.tlcn.sportsnet_backend.dto.tournament;

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
    private String name;
    private String description;
    private String location;
    private String bannerUrl;
    private String logoUrl;
    private BigDecimal fee;
    private String rules;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationStartDate;
    private LocalDateTime registrationEndDate;

    // Danh sách các hạng mục thi đấu (category) trong giải này
    private List<TournamentCategoryRequest> categories;
}
