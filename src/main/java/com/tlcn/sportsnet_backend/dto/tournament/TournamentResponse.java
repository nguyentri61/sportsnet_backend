package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.enums.TournamentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TournamentResponse {

    private String id;
    private String name;
    private String description;
    private String location;
    private String slug;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime registrationStartDate;

    private LocalDateTime registrationEndDate;

    private String logoUrl;
    private String bannerUrl;

    private Instant createdAt;
    private TournamentStatus status;

    private String createdBy;
    private List<TournamentCategoryResponse> categories;
}
