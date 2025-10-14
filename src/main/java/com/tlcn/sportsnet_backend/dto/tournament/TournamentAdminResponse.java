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
public class TournamentAdminResponse {
    private String id;
    private String name;
    private String location;
    private FacilityResponse facility;
    private String slug;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationStartDate;
    private LocalDateTime registrationEndDate;
    private Instant createdAt;
    private TournamentStatus status;
    private BigDecimal fee;
    private List<TournamentCategoryResponse> categories;
}
