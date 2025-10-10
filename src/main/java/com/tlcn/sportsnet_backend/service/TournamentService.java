package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club.MyClubResponse;
import com.tlcn.sportsnet_backend.dto.tournament.TournamentCategoryRequest;
import com.tlcn.sportsnet_backend.dto.tournament.TournamentCategoryResponse;
import com.tlcn.sportsnet_backend.dto.tournament.TournamentCreateRequest;
import com.tlcn.sportsnet_backend.dto.tournament.TournamentResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.TournamentStatus;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    public TournamentResponse createTournament(TournamentCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Set<Role> roles = account.getRoles();
        if(roles.stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new InvalidDataException("You are not an admin");
        }
        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .description(request.getDescription())
                .bannerUrl(request.getBannerUrl())
                .logoUrl(request.getLogoUrl())
                .location(request.getLocation())
                .registrationEndDate(request.getRegistrationEndDate())
                .registrationStartDate(request.getRegistrationStartDate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(TournamentStatus.UPCOMING)
                .build();
        tournament = tournamentRepository.save(tournament);
        List<TournamentCategory> tournamentCategories = new ArrayList<>();
        List<TournamentCategoryRequest> tournamentCategoryRequests = request.getCategories();
        for (TournamentCategoryRequest tournamentCategoryRequest : tournamentCategoryRequests) {
            TournamentCategory tournamentCategory = TournamentCategory.builder()
                    .category(tournamentCategoryRequest.getCategoryType())
                    .tournament(tournament)
                    .maxLevel(tournamentCategoryRequest.getMaxLevel())
                    .minLevel(tournamentCategoryRequest.getMinLevel())
                    .maxParticipants(tournamentCategoryRequest.getMaxParticipants())
                    .build();
            tournamentCategories.add(tournamentCategory);

        }
        tournamentCategoryRepository.saveAll(tournamentCategories);
        tournament.setCategories(tournamentCategories);
        return toTournamentResponse(tournament);

    }
    public PagedResponse<TournamentResponse> getAllTournament(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Page<Tournament> tournamentPage = tournamentRepository.findAll(pageable);
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
                .registrationEndDate(tournament.getRegistrationEndDate())
                .registrationStartDate(tournament.getRegistrationStartDate())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .build();
    }


}
