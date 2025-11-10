package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.facility.FacilityResponse;
import com.tlcn.sportsnet_backend.dto.tournament.*;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.TournamentStatus;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.FacilityRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class TournamentService {
    private final TournamentRepository tournamentRepository;
    private final TournamentCategoryRepository tournamentCategoryRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final FacilityRepository facilityRepository;

    public TournamentResponse createTournament(TournamentCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Set<Role> roles = account.getRoles();
        if(roles.stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new InvalidDataException("You are not an admin");
        }

        Facility facility = null;
        if (request.getFacilityId() != null && !request.getFacilityId().isBlank()) {
            facility = facilityRepository.findById(request.getFacilityId())
                    .orElseThrow(() -> new InvalidDataException("Facility not found"));
        }

        if (facility == null && (request.getLocation() == null || request.getLocation().isBlank())) {
            throw new InvalidDataException("Cần chọn cơ sở hoặc nhập địa điểm tùy chỉnh");
        }

        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bannerUrl(request.getBannerUrl())
                .logoUrl(request.getLogoUrl())
                .location(request.getLocation())
                .facility(facility)
                .registrationEndDate(request.getRegistrationEndDate())
                .registrationStartDate(request.getRegistrationStartDate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TournamentStatus.UPCOMING)
                .fee(request.getFee())
                .rules(request.getRules())
                .build();
        tournament = tournamentRepository.save(tournament);

        List<TournamentCategory> tournamentCategories = new ArrayList<>();

        for (TournamentCategoryRequest c : request.getCategories()) {
            TournamentCategory category = TournamentCategory.builder()
                    .category(c.getCategoryType())
                    .tournament(tournament)
                    .minLevel(c.getMinLevel())
                    .maxLevel(c.getMaxLevel())
                    .maxParticipants(c.getMaxParticipants())
                    .registrationFee(c.getRegistrationFee())
                    .description(c.getDescription())
                    .rules(c.getRules())
                    .firstPrize(c.getFirstPrize())
                    .secondPrize(c.getSecondPrize())
                    .thirdPrize(c.getThirdPrize())
                    .format(c.getFormat())
                    .registrationDeadline(c.getRegistrationDeadline())
                    .build();

            tournamentCategories.add(category);
        }

        tournamentCategoryRepository.saveAll(tournamentCategories);
        tournament.setCategories(tournamentCategories);
        return toTournamentResponse(tournament);
    }
    public PagedResponse<TournamentResponse> getAllTournament(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null);
        Page<Tournament> tournamentPage = tournamentRepository.findAllByStatusNot(pageable, TournamentStatus.CANCELLED);
        List<TournamentResponse> content = new ArrayList<>();
        for (Tournament tournament : tournamentPage) {
            content.add(toTournamentResponse(tournament));
        }
        return new PagedResponse<>(
                content,
                tournamentPage.getNumber(),
                tournamentPage.getSize(),
                tournamentPage.getTotalElements(),
                tournamentPage.getTotalPages(),
                tournamentPage.isLast()
        );
    }

    public TournamentDetailResponse getBySlug(String slug) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null);
        Tournament tournament = tournamentRepository.findBySlug(slug).orElseThrow(() -> new InvalidDataException("Tournament not found"));
        return tournamentDetailResponse(tournament);
    }

    public TournamentResponse toTournamentResponse(Tournament tournament) {
        List<TournamentCategory> tournamentCategories = tournament.getCategories();
        List<TournamentCategoryResponse> tournamentCategoryResponses = new ArrayList<>();
        for (TournamentCategory tournamentCategory : tournamentCategories) {
            TournamentCategoryResponse tournamentCategoryResponse = TournamentCategoryResponse.builder()
                    .category(tournamentCategory.getCategory())
                    .id(tournamentCategory.getId())
                    .currentParticipantCount(0)
                    .maxParticipants(tournamentCategory.getMaxParticipants())
                    .build();
            tournamentCategoryResponses.add(tournamentCategoryResponse);
        }
        return TournamentResponse.builder()
                .createdAt(tournament.getCreatedAt())
                .slug(tournament.getSlug())
                .createdBy(tournament.getCreatedBy())
                .status(tournament.getStatus())
                .categories(tournamentCategoryResponses)
                .bannerUrl(fileStorageService.getFileUrl(tournament.getBannerUrl(), "/tournament"))
                .logoUrl(fileStorageService.getFileUrl(tournament.getLogoUrl(), "/tournament"))
                .location(tournament.getLocation())
                .facility(tournament.getFacility() != null ? toFacilityResponse(tournament.getFacility()) : null)
                .registrationEndDate(tournament.getRegistrationEndDate())
                .registrationStartDate(tournament.getRegistrationStartDate())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .fee(tournament.getFee())
                .build();
    }

    public TournamentDetailResponse tournamentDetailResponse(Tournament tournament){
        List<TournamentCategory> tournamentCategories = tournament.getCategories();
        List<TournamentCategoryDetailResponse> tournamentCategoryResponses = new ArrayList<>();
        for (TournamentCategory tournamentCategory : tournamentCategories) {
            TournamentCategoryDetailResponse tournamentCategoryResponse = TournamentCategoryDetailResponse.builder()
                    .category(tournamentCategory.getCategory())
                    .id(tournamentCategory.getId())
                    .currentParticipantCount(0)
                    .maxParticipants(tournamentCategory.getMaxParticipants())
                    .minLevel(tournamentCategory.getMinLevel())
                    .maxLevel(tournamentCategory.getMaxLevel())
                    .build();
            tournamentCategoryResponses.add(tournamentCategoryResponse);
        }
        return TournamentDetailResponse.builder()
                .createdAt(tournament.getCreatedAt())
                .slug(tournament.getSlug())
                .createdBy(tournament.getCreatedBy())
                .status(tournament.getStatus())
                .rules(tournament.getRules())
                .categories(tournamentCategoryResponses)
                .bannerUrl(fileStorageService.getFileUrl(tournament.getBannerUrl(), "/tournament"))
                .logoUrl(fileStorageService.getFileUrl(tournament.getLogoUrl(), "/tournament"))
                .location(tournament.getLocation())
                .facility(tournament.getFacility() != null ? toFacilityResponse(tournament.getFacility()) : null)
                .registrationEndDate(tournament.getRegistrationEndDate())
                .registrationStartDate(tournament.getRegistrationStartDate())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .fee(tournament.getFee())
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

    private void calculateStatus(Tournament tournament) {
        TournamentStatus oldStatus = tournament.getStatus();
        TournamentStatus status =tournament.getStatus();
        LocalDateTime now = LocalDateTime.now();
        if(now.isAfter(tournament.getRegistrationStartDate()) && now.isBefore(tournament.getRegistrationEndDate())) {
            status = TournamentStatus.REGISTRATION_OPEN;
        } else if (now.isAfter(tournament.getRegistrationEndDate()) && now.isBefore(tournament.getStartDate())) {
            status = TournamentStatus.REGISTRATION_CLOSED;
        } else if (now.isAfter(tournament.getStartDate()) && now.isBefore(tournament.getEndDate())) {
            status = TournamentStatus.IN_PROGRESS;
        } else if (now.isAfter(tournament.getEndDate()) && oldStatus!=TournamentStatus.CANCELLED) {
            status = TournamentStatus.COMPLETED;
        }
        if(oldStatus!=status) {
            tournament.setStatus(status);
            tournamentRepository.save(tournament);
        }
    }
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoUpdateTournamentStatus() {
        System.out.println("Chạy hàm giải đấu");
        List<TournamentStatus> excludedStatuses = List.of(
                TournamentStatus.CANCELLED,
                TournamentStatus.COMPLETED
        );
        List<Tournament> tournaments = tournamentRepository.findAllByStatusNot(excludedStatuses);
        for (Tournament tournament : tournaments) {
            calculateStatus(tournament);
        }
    }


}
