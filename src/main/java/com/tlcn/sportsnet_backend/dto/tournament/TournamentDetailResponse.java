package com.tlcn.sportsnet_backend.dto.tournament;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.enums.TournamentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentDetailResponse {
    private String id;
    private String name;
    private String description;
    private String location;
    private FacilityResponse facility;
    private String slug;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime registrationStartDate;

    private LocalDateTime registrationEndDate;

    private String logoUrl;
    private String bannerUrl;
    private String rules;
    private Instant createdAt;
    private TournamentStatus status;
    private BigDecimal fee;
    private String createdBy;
    private List<TournamentCategoryDetailResponse> categories;
    private List<TournamentPlayerResponse> players;
}
