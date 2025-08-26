package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club.ClubCreateRequest;
import com.tlcn.sportsnet_backend.dto.club.ClubResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.Role;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import com.tlcn.sportsnet_backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final FileStorageService fileStorageService;

    public ClubResponse createClub(ClubCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account owner = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Owner not found"));

        Role ownerRole = roleRepository.findByName("ROLE_CLUB_OWNER")
                .orElseThrow(() -> new InvalidDataException("Role OWNER_CLUB not found"));

        owner.setRoles(Set.of(ownerRole));
        roleRepository.save(ownerRole);

        Club club = Club.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .location(request.getLocation())
                .maxMembers(request.getMaxMembers())
                .visibility(request.getVisibility())
                .tags(request.getTags() != null ? request.getTags() : new HashSet<>())
                .owner(owner)
                .build();

        club = clubRepository.save(club);

        return toClubResponse(club);
    }

    public void activateClub(String id) {
        Club club = clubRepository.findById(id).orElseThrow(() -> new InvalidDataException("Club not found"));
        club.setActive(true);

        Account account = club.getOwner();
        Role ownerRole = roleRepository.findByName("ROLE_CLUB_OWNER")
                .orElseThrow(() -> new InvalidDataException("Role OWNER_CLUB not found"));

        account.getRoles().add(ownerRole);

        clubRepository.save(club);
        accountRepository.save(account);
    }

    public ClubResponse getClubInformation(String id) {
        Club club = clubRepository.findById(id).orElseThrow(() -> new InvalidDataException("Club not found"));
        return toClubResponse(club);
    }

    private ClubResponse toClubResponse(Club club) {
        return ClubResponse.builder()
                .id(club.getId())
                .name(club.getName())
                .description(club.getDescription())
                .logoUrl(fileStorageService.getFileUrl(club.getLogoUrl(), "/club/logo"))
                .location(club.getLocation())
                .maxMembers(club.getMaxMembers())
                .visibility(club.getVisibility())
                .tags(club.getTags())
                .active(club.isActive())
                .ownerName(club.getOwner().getUserInfo().getFullName())
                .createdAt(club.getCreatedAt())
                .build();
    }
}
