package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.dto.tournament.TournamentCategoryDetailResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Facility;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.enums.BadmintonCategoryEnum;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.FacilityRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentCategoryService {
    private final TournamentCategoryRepository tournamentCategoryRepository;
    private final FileStorageService fileStorageService;
    private final AccountRepository accountRepository;
    private final TournamentParticipantRepository tournamentParticipantRepository;

    public TournamentCategoryDetailResponse getDetailCategoryById(String categoryId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null);

        TournamentCategory category = tournamentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return toResponse(category, account);
    }

    private TournamentCategoryDetailResponse toResponse(TournamentCategory tournamentCategory, Account account) {

        int currentCount = (int) tournamentCategory.getParticipants().stream()
                .filter(p -> p.getStatus() == TournamentParticipantEnum.APPROVED)
                .count();

        boolean isAdmin = false;
        TournamentParticipant tournamentParticipant = null;

        if (account != null) {
            isAdmin = account.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

            tournamentParticipant = tournamentParticipantRepository.findByAccountAndCategory(account, tournamentCategory);
        }

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
                .isDouble(tournamentCategory.getCategory() != BadmintonCategoryEnum.MEN_SINGLE && tournamentCategory.getCategory()!= BadmintonCategoryEnum.WOMEN_SINGLE)
                .participantStatus(tournamentParticipant != null ? tournamentParticipant.getStatus() : null)
                .admin(isAdmin)
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
