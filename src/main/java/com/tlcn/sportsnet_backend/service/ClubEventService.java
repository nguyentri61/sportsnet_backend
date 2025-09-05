package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.*;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ParticipantRoleEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubEventService {
    private final ClubRepository clubRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final AccountRepository accountRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final FileStorageService fileStorageService;

    public ClubEventCreateResponse createClubEvent(ClubEventCreateRequest request) {
        Club club = clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new InvalidDataException("Club not found"));

        ClubEvent event = ClubEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .image(request.getImage())
                .requirements(request.getRequirements())
                .location(request.getLocation())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalMember(request.getTotalMember())
                .categories(request.getType())
                .status(EventStatusEnum.OPEN) // mặc định
                .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                .deadline(request.getDeadline() != null ? request.getDeadline() : request.getStartTime().minusDays(1))
                .openForOutside(request.isOpenForOutside()) // mặc định không mở cho người ngoài
                .maxClubMembers(request.getMaxClubMembers() > 0 ? request.getMaxClubMembers() : request.getTotalMember())
                .maxOutsideMembers(request.getMaxOutsideMembers()) // có thể 0
                .club(club)
                .build();

        clubEventRepository.save(event);

        return toClubEventCreateResponse(event);
    }

    public ClubEventDetailResponse getEventClubInfo(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null);
        ClubEvent event = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Club not found"));
        return toClubEventDetailResponse(event, account);
    }

    public PagedResponse<ClubEventResponse> getAllEventsByClubId(String clubId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClubEvent> events = clubEventRepository.findByClub_Id(clubId, pageable);

        List<ClubEventResponse> content = events.getContent().stream()
                .map(this::toClubEventResponse)
                .toList();

        return new PagedResponse<>(
                content,
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isLast()
        );
    }
    public PagedResponse<ClubEventResponse> getAllPublicEventClub( int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ClubEvent> events = clubEventRepository.findAllByOpenForOutsideAndStatusAndDeadlineAfter( pageable, true,  EventStatusEnum.OPEN, LocalDateTime.now());
        List<ClubEventResponse> content = events.getContent().stream()
                .map(this::toClubEventResponse)
                .toList();

        return new PagedResponse<>(
                content,
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isLast()
        );
    }
    public PagedResponse<ClubEventResponse>  getAllMyClubEventClub(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Page<ClubEvent> events = clubEventRepository.findByClub_Members_Account_IdAndClub_Members_StatusAndStatusAndDeadlineAfter( account.getId(), ClubMemberStatusEnum.APPROVED,  EventStatusEnum.OPEN, LocalDateTime.now(), pageable);
        List<ClubEventResponse> content = events.getContent().stream()
                .map(this::toClubEventResponse)
                .toList();

        return new PagedResponse<>(
                content,
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isLast()
        );
    }
    private ClubEventCreateResponse toClubEventCreateResponse(ClubEvent event) {
        return ClubEventCreateResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .requirements(event.getRequirements())
                .image(fileStorageService.getFileUrl(event.getImage(), "/club/events"))
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .totalMember(event.getTotalMember())
                .categories(event.getCategories())
                .status(event.getStatus())
                .fee(event.getFee())
                .deadline(event.getDeadline())
                .openForOutside(event.isOpenForOutside())
                .maxClubMembers(event.getMaxClubMembers())
                .maxOutsideMembers(event.getMaxOutsideMembers())
                .clubId(event.getClub().getId())
                .createdAt(event.getCreatedAt())
                .createdBy(event.getCreatedBy())
                .build();
    }

    private ClubEventResponse toClubEventResponse(ClubEvent event) {
        event = calculateStatus(event);
        return ClubEventResponse.builder()
                .id(event.getId())
                .image(fileStorageService.getFileUrl(event.getImage(), "/club/events"))
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .title(event.getTitle())
                .fee(event.getFee())
                .joinedMember(event.getParticipants().size())
                .totalMember(event.getTotalMember())
                .nameClub(event.getClub().getName())
                .categories(event.getCategories())
                .openForOutside(event.isOpenForOutside())
                .status(event.getStatus())
                .build();
    }

    private ClubEventDetailResponse toClubEventDetailResponse(ClubEvent event, Account account) {
        Club club = event.getClub();
        event = calculateStatus(event);
        ParticipantRoleEnum roleEnum= ParticipantRoleEnum.GUEST;
        if(account!=null) {
            if (club.getOwner().equals(account)){
                roleEnum = ParticipantRoleEnum.OWNER;
            }
            else if(clubMemberRepository.existsByClubAndAccount(club, account)){
                roleEnum = ParticipantRoleEnum.MEMBER;
            }
        }
        return ClubEventDetailResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .requirements(event.getRequirements())
                .image(fileStorageService.getFileUrl(event.getImage(), "/club/events"))
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .totalMember(event.getTotalMember())
                .joinedMember(event.getParticipants().size())
                .categories(event.getCategories())
                .status(event.getStatus())
                .fee(event.getFee())
                .deadline(event.getDeadline())
                .openForOutside(event.isOpenForOutside())
                .maxClubMembers(event.getMaxClubMembers())
                .maxOutsideMembers(event.getMaxOutsideMembers())
                .clubId(event.getClub().getId())
                .createdAt(event.getCreatedAt())
                .createdBy(event.getCreatedBy())
                .nameClub(event.getClub().getName())
                .isJoined(clubEventParticipantRepository.existsByClubEventAndParticipant(event, account))
                .participantRole(roleEnum)
                .build();
    }
    public ClubEvent calculateStatus(ClubEvent event) {
        EventStatusEnum newStatus = event.getStatus();

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(event.getDeadline()) && now.isBefore(event.getStartTime())) {
            newStatus = EventStatusEnum.CLOSED;
        } else if (now.isAfter(event.getStartTime()) && now.isBefore(event.getEndTime())) {
            newStatus = EventStatusEnum.ONGOING;
        } else if (now.isAfter(event.getEndTime())) {
            newStatus = EventStatusEnum.FINISHED;
        }

        // Nếu status thay đổi, lưu vào DB
        if (!event.getStatus().equals(newStatus)) {
            event.setStatus(newStatus);
            event = clubEventRepository.save(event);
        }
        return event;
    }


    public ClubEventDetailResponse updateClubEvent(String id, ClubEventUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        ClubEvent clubEvent = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Club not found"));

        clubEvent.setTitle(request.getTitle());
        clubEvent.setDescription(request.getDescription());
        clubEvent.setRequirements(request.getRequirements());
        clubEvent.setImage(request.getImage() != null ? request.getImage() : "");
        clubEvent.setLocation(request.getLocation());
        clubEvent.setStartTime(request.getStartTime());
        clubEvent.setEndTime(request.getEndTime());
        clubEvent.setDeadline(request.getDeadline());
        clubEvent.setOpenForOutside(request.isOpenForOutside());
        clubEvent.setStatus(request.getStatus());
        clubEvent.setFee(request.getFee());
        clubEvent.setDeadline(request.getDeadline());
        clubEvent.setCategories(request.getCategories());

        clubEvent = clubEventRepository.save(clubEvent);

        return toClubEventDetailResponse(clubEvent, account);
    }
}
