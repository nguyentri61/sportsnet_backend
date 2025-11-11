package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.dto.tournament.TournamentCategoryDetailResponse;
import com.tlcn.sportsnet_backend.entity.Facility;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.repository.FacilityRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentCategoryService {
    private final TournamentCategoryRepository tournamentCategoryRepository;
    private final FileStorageService fileStorageService;

    public TournamentCategoryDetailResponse getDetailCategoryById(String categoryId) {

        TournamentCategory category = tournamentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return toResponse(category);
    }

    private TournamentCategoryDetailResponse toResponse(TournamentCategory tournamentCategory) {

        int currentCount = (int) tournamentCategory.getParticipants().stream()
                .filter(p -> p.getStatus() == TournamentParticipantEnum.APPROVED)
                .count();

        return TournamentCategoryDetailResponse.builder()
                .id(tournamentCategory.getId())
                .tournamentName(tournamentCategory.getTournament().getName())
                .facility(tournamentCategory.getTournament().getFacility() != null ? toFacilityResponse(tournamentCategory.getTournament().getFacility()) : null)
                .startDate(tournamentCategory.getTournament().getStartDate())
                .endDate(tournamentCategory.getTournament().getEndDate())
                .category(tournamentCategory.getCategory())
                .minLevel(tournamentCategory.getMinLevel())
                .maxLevel(tournamentCategory.getMaxLevel())
                .maxParticipants(tournamentCategory.getMaxParticipants())
                .currentParticipantCount(currentCount)
                .registrationFee(tournamentCategory.getRegistrationFee())
                .description(tournamentCategory.getDescription())
                .rules(tournamentCategory.getRules())
                .firstPrize(tournamentCategory.getFirstPrize())
                .secondPrize(tournamentCategory.getSecondPrize())
                .thirdPrize(tournamentCategory.getThirdPrize())
                .format(tournamentCategory.getFormat().name())
                .registrationDeadline(tournamentCategory.getRegistrationDeadline())
                .build();
    }

    private FacilityResponse toFacilityResponse(Facility facility) {
        return FacilityResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .district(facility.getDistrict())
                .city(facility.getCity())
                .location(facility.getLocation())
                .latitude(facility.getLatitude())
                .longitude(facility.getLongitude())
                .image(fileStorageService.getFileUrl(facility.getImage(), "/facility"))
                .build();
    }
}
