package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club.ClubAdminResponse;
import com.tlcn.sportsnet_backend.dto.club.ClubCreateRequest;
import com.tlcn.sportsnet_backend.dto.club.ClubResponse;
import com.tlcn.sportsnet_backend.dto.club.MyClubResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.*;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;


import java.security.Permission;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ClubEventRatingRepository clubEventRatingRepository;
    private final FileStorageService fileStorageService;
    private final ClubEventRepository clubEventRepository;
    private final ClubEventParticipantRepository clubEventParticipantRepository;

    public ClubResponse createClub(ClubCreateRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account owner = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Owner not found"));

        Role ownerRole = roleRepository.findByName("ROLE_CLUB_OWNER")
                .orElseThrow(() -> new InvalidDataException("Role OWNER_CLUB not found"));

        Set<Role> roles = owner.getRoles();
        roles.add(ownerRole);
        owner.setRoles(roles);
        roleRepository.save(ownerRole);

        Club club = Club.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .location(request.getLocation())
                .maxMembers(request.getMaxMembers())
                .minLevel(request.getMinLevel())
                .maxLevel(request.getMaxLevel())
                .visibility(request.getVisibility())
                .tags(request.getTags() != null ? request.getTags() : new HashSet<>())
                .owner(owner)
                .build();

        club = clubRepository.save(club);
        ClubMember clubMember = ClubMember.builder()
                .club(club)
                .account(owner)
                .role(ClubMemberRoleEnum.OWNER)
                .status(ClubMemberStatusEnum.APPROVED)
                .joinedAt(Instant.now())
                .build();

        clubMemberRepository.save(clubMember);
        return toClubResponse(club);
    }
    public PagedResponse<ClubResponse> getAllClubPublic(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElse(null);

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("reputation"),
                Sort.Order.desc("createdAt")
        ));
        Page<Club> clubs;

        if (account != null) {
            clubs = clubRepository.findAvailableClubsForUserAndStatus(ClubVisibilityEnum.PUBLIC, ClubStatusEnum.ACTIVE, account, pageable);
        } else {
            clubs = clubRepository.findAllByVisibilityAndStatus(ClubVisibilityEnum.PUBLIC, ClubStatusEnum.ACTIVE, pageable);
        }
        List<ClubResponse> content = clubs.stream()
                .map(this::toClubResponse)
                .toList();

        return new PagedResponse<>(
                content,
                clubs.getNumber(),
                clubs.getSize(),
                clubs.getTotalElements(),
                clubs.getTotalPages(),
                clubs.isLast()
        );
    }

    public PagedResponse<MyClubResponse> getAllMyClub(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Club> clubs = clubRepository.findAvailableClubsBelongUserAndStatus( account, ClubStatusEnum.ACTIVE, pageable);

        List<MyClubResponse> content = new ArrayList<>();
        for (Club club : clubs) {
            content.add(toMyClubResponse(club, account));
        }
// Sắp xếp: owner lên đầu
        content.sort((c1, c2) -> Boolean.compare(!c1.isOwner(), !c2.isOwner()));
        return new PagedResponse<>(
                content,
                clubs.getNumber(),
                clubs.getSize(),
                clubs.getTotalElements(),
                clubs.getTotalPages(),
                clubs.isLast()
        );
    }
    public MyClubResponse getMyClubInformation(String slug) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        System.out.println(account.getEmail());
        Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new InvalidDataException("Club not found"));
        System.out.println(club.getName());
        ClubMember clubMember = clubMemberRepository.findClubMemberByAccountAndClub(account, club);
//        System.out.println(clubMember.toString());
        if(clubMember.getStatus() != ClubMemberStatusEnum.APPROVED) {
            throw new InvalidDataException("ClubMember is not approved");
        }
        return toMyClubResponse(club, account);
    }
    public void updateClubStatus(String id, ClubStatusEnum newStatus) {
        Club club = clubRepository.findBySlug(id).orElseThrow(() -> new InvalidDataException("Club not found"));

        club.setStatus(newStatus);

        if(newStatus == ClubStatusEnum.ACTIVE) {
            Account account = club.getOwner();
            Role ownerRole = roleRepository.findByName("ROLE_CLUB_OWNER")
                    .orElseThrow(() -> new InvalidDataException("Role OWNER_CLUB not found"));

            account.getRoles().add(ownerRole);
            clubRepository.save(club);
            accountRepository.save(account);
        }

        clubRepository.save(club);
    }

    public ClubResponse getClubInformation(String slug) {
        Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new InvalidDataException("Club not found"));
        return toClubResponse(club);
    }

    private ClubResponse toClubResponse(Club club) {
        List<ClubMember> members = clubMemberRepository.findByClubIdAndStatus(club.getId(), ClubMemberStatusEnum.APPROVED);
        return ClubResponse.builder()
                .id(club.getId())
                .slug(club.getSlug())
                .name(club.getName())
                .description(club.getDescription())
                .logoUrl(fileStorageService.getFileUrl(club.getLogoUrl(), "/club/logo"))
                .location(club.getLocation())
                .memberCount(members.size())
                .maxMembers(club.getMaxMembers())
                .minLevel(club.getMinLevel())
                .maxLevel(club.getMaxLevel())
                .visibility(club.getVisibility())
                .tags(club.getTags())
                .status(club.getStatus())
                .ownerName(club.getOwner().getUserInfo().getFullName())
                .createdAt(club.getCreatedAt())
                .build();
    }

    private MyClubResponse toMyClubResponse(Club club, Account account) {

        ClubMember clubMember = clubMemberRepository.findByClubAndAccount(club, account);
        Instant joinAt = club.getCreatedAt();
        if(clubMember != null) {
            joinAt =clubMember.getJoinedAt();
        }
        List<ClubMember> members = clubMemberRepository.findByClubIdAndStatus(club.getId(), ClubMemberStatusEnum.APPROVED);
        ClubMember member = clubMemberRepository.findClubMemberByAccountAndClub(account, club);
        assert member != null;
        return MyClubResponse.builder()
                .id(club.getId())
                .slug(club.getSlug())
                .name(club.getName())
                .description(club.getDescription())
                .logoUrl(fileStorageService.getFileUrl(club.getLogoUrl(), "/club/logo"))
                .location(club.getLocation())
                .maxMembers(club.getMaxMembers())
                .visibility(club.getVisibility())
                .tags(club.getTags())
                .status(club.getStatus())
                .memberStatus(member.getStatus())
                .ownerName(club.getOwner().getUserInfo().getFullName())
                .createdAt(club.getCreatedAt())
                .dateJoined(joinAt)
                .memberCount(members.size())
                .minLevel(club.getMinLevel())
                .maxLevel(club.getMaxLevel())
                .isOwner(club.getOwner()==account)
                .build();
    }

    private ClubAdminResponse toClubAdminResponse(Club club) {
        List<ClubMember> members = clubMemberRepository.findByClubIdAndStatus(club.getId(), ClubMemberStatusEnum.APPROVED);

        return ClubAdminResponse.builder()
                .id(club.getId())
                .slug(club.getSlug())
                .email(club.getCreatedBy())
                .name(club.getName())
                .maxMembers(club.getMaxMembers())
                .status(club.getStatus())
                .ownerName(club.getOwner().getUserInfo().getFullName())
                .createdAt(club.getCreatedAt())
                .memberCount(members.size())
                .build();
    }


    public PagedResponse<ClubAdminResponse> getAllClubs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Club> clubs = clubRepository.findAll(pageable);

        List<ClubAdminResponse> content = clubs.stream()
                .map(this::toClubAdminResponse)
                .toList();

        return new PagedResponse<>(
                content,
                clubs.getNumber(),
                clubs.getSize(),
                clubs.getTotalElements(),
                clubs.getTotalPages(),
                clubs.isLast()
        );
    }

    public void deleteClub(String id) {
        Club club = clubRepository.findBySlug(id).orElseThrow(() -> new InvalidDataException("Club not found"));
        clubRepository.delete(club);
    }

    public double calculateReputation(Club club) {
        // ----- 1. Weighted Event Rating -----
        double weightedEventRating = clubEventRatingRepository.getWeightedAverageRatingByClubId(club.getId());
        if (Double.isNaN(weightedEventRating)) weightedEventRating = 0.0;
        // ----- 2. Activity Score -----
        long countEvent = clubEventRepository.countByClubIdAndStatus(club.getId(), EventStatusEnum.FINISHED);
        Long maxCountEventLong = clubEventRepository.findMaxEventCount(EventStatusEnum.FINISHED);
        long maxCountEvent = (maxCountEventLong != null) ? maxCountEventLong : 0L;

        double activityScore = 0.0;
        if (maxCountEvent > 0) {
            activityScore = Math.log(1 + countEvent) / Math.log(1 + maxCountEvent) * 100;
            if (Double.isNaN(activityScore) || Double.isInfinite(activityScore)) activityScore = 0.0;
        }
        // ----- 3. Engagement Score -----
        long totalApprovedParticipants = clubEventParticipantRepository.countByClubIdAndStatus(club.getId(), ClubEventParticipantStatusEnum.APPROVED);
        Long totalEventCapacityLong = clubEventRepository.sumTotalMemberByClubId(club.getId());
        long totalEventCapacity = (totalEventCapacityLong != null) ? totalEventCapacityLong : 0L;

        double engagementScore = 0.0;
        if (totalEventCapacity > 0) {
            engagementScore = (double) totalApprovedParticipants / totalEventCapacity * 100;
            if (Double.isNaN(engagementScore) || Double.isInfinite(engagementScore)) engagementScore = 0.0;
        }


        // ----- 4. Longevity Score -----
        int currentYear = ZonedDateTime.now(ZoneId.systemDefault()).getYear();
        int createdYear = ZonedDateTime.ofInstant(club.getCreatedAt(), ZoneId.systemDefault()).getYear();
        int yearsSinceFounded = Math.max(currentYear - createdYear, 0); // tránh âm
        int longevityScore = Math.min(yearsSinceFounded * 10, 20);


        // ----- 5. Tính tổng uy tín -----
        double clubReputation = 0.4 * (weightedEventRating / 5) * 100
                + 0.2 * activityScore
                + 0.2 * engagementScore
                + 0.2 * longevityScore;


        // ----- 6. Lưu vào DB -----
        club.setReputation(clubReputation);
        clubRepository.save(club);

        return clubReputation;
    }



    public void calculateAllClubReputationOnStartup() {
        List<Club> clubs = clubRepository.findAll();
        for (Club club : clubs) {
            double reputation = calculateReputation(club);
            System.out.println("CLB: " + club.getName() + " - Reputation: " + reputation);
            // Bạn có thể lưu vào DB nếu cần
        }
    }
}
