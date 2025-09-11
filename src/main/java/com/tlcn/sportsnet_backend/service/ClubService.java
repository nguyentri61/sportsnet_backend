package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club.ClubAdminResponse;
import com.tlcn.sportsnet_backend.dto.club.ClubCreateRequest;
import com.tlcn.sportsnet_backend.dto.club.ClubResponse;
import com.tlcn.sportsnet_backend.dto.club.MyClubResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.entity.Role;
import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import com.tlcn.sportsnet_backend.repository.RoleRepository;
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
    private final FileStorageService fileStorageService;

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

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Club> clubs;

        if (account != null) {
            clubs = clubRepository.findAvailableClubsForUser(ClubVisibilityEnum.PUBLIC, account, pageable);
        } else {
            clubs = clubRepository.findAllByVisibility(ClubVisibilityEnum.PUBLIC, pageable);
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
        Page<Club> clubs = clubRepository.findAvailableClubsBelongUser( account, pageable);

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
        return ClubResponse.builder()
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
}
