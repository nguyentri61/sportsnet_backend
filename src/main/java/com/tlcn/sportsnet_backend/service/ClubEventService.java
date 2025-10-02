package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.*;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.enums.ParticipantRoleEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubEventService {
    private final ClubRepository clubRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final AbsentReasonRepository absentReasonRepository;
    private final AccountRepository accountRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final ClubService clubService;

    @Transactional
    public ClubEventCreateResponse createClubEvent(ClubEventCreateRequest request) {
        Club club = clubRepository.findBySlug(request.getClubSlug())
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
                .status(EventStatusEnum.DRAFT) // mặc định
                .fee(request.getFee() != null ? request.getFee() : BigDecimal.ZERO)
                .deadline(request.getDeadline() != null ? request.getDeadline() : request.getStartTime().minusDays(1))
                .openForOutside(request.isOpenForOutside()) // mặc định không mở cho người ngoài
                .maxClubMembers(request.getMaxClubMembers() > 0 ? request.getMaxClubMembers() : request.getTotalMember())
                .maxOutsideMembers(request.getMaxOutsideMembers()) // có thể 0
                .club(club)
                .minLevel(request.getMinLevel())
                .maxLevel(request.getMaxLevel())
                .build();

        event = clubEventRepository.save(event);
        notificationService.sendToClub(club.getId(),"Hoạt động mới","Câu lạc bộ: "+club.getName()+ " đã tạo hoạt động mới","/events/"+event.getSlug());
        return toClubEventCreateResponse(event);
    }

    public ClubEventDetailResponse getEventClubInfo(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null);
        ClubEvent event = clubEventRepository.findBySlug(id).orElseThrow(() -> new InvalidDataException("Club not found"));
        return toClubEventDetailResponse(event, account);
    }

    public PagedResponse<ClubEventResponse> getAllEventsByClubId(String clubSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ClubEvent> events = clubEventRepository.findByClub_Slug(clubSlug, pageable);

        List<ClubEventResponse> content = events.getContent().stream()
                .map(event -> toClubEventResponse(event, null))
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
    public PagedResponse<ClubEventResponse> getAllPublicEventClub( int page, int size, String search, String province,String ward,
                                                                   String quickTimeFilter, Boolean isFree, BigDecimal minFee, BigDecimal maxFee,
                                                                   LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElse(null); 
        Page<ClubEvent> events = clubEventRepository.findAllByOpenForOutsideAndStatusAndDeadlineAfter( pageable, true,  EventStatusEnum.OPEN, LocalDateTime.now());

        // Lọc dữ liệu theo search, province và ward
        List<ClubEvent> filteredEvents = events.getContent().stream()
                .filter(event -> matchesSearch(event, search))
                .filter(event -> matchesProvince(event, province))
                .filter(event -> matchesWard(event, ward))
                .filter(event -> matchesQuickTimeFilter(event, quickTimeFilter))
                .filter(event -> matchesFeeFilter(event, isFree, minFee, maxFee))
                .filter(event -> matchesDateRange(event, startDate, endDate))
                .toList();

        Page<ClubEvent> filteredPage = new PageImpl<>(
                filteredEvents,
                pageable,
                filteredEvents.size()
        );

        List<ClubEventResponse> content = filteredPage.getContent().stream()
                .map(event -> toClubEventResponse(event, account))
                .toList();

        return new PagedResponse<>(
                content,
                filteredPage.getNumber(),
                filteredPage.getSize(),
                filteredPage.getTotalElements(),
                filteredPage.getTotalPages(),
                filteredPage.isLast()
        );
    }
    public PagedResponse<ClubEventResponse>  getAllMyClubEventClub(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Page<ClubEvent> events = clubEventRepository.findByClub_Members_Account_IdAndClub_Members_StatusAndStatusAndDeadlineAfter( account.getId(), ClubMemberStatusEnum.APPROVED,  EventStatusEnum.OPEN, LocalDateTime.now(), pageable);
        List<ClubEventResponse> content = events.getContent().stream()
                .map(event -> toClubEventResponse(event, account))
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

    public PagedResponse<ClubEventResponse> getAllMyJoinedClubEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));

        Page<ClubEventParticipant> participants = clubEventParticipantRepository.findByParticipant_Id(account.getId(), pageable);

        List<ClubEventResponse> content = participants.getContent().stream()
                .map(participant -> toClubEventResponse(participant.getClubEvent(), account))
                .toList();

        return new PagedResponse<>(
                content,
                participants.getNumber(),
                participants.getSize(),
                participants.getTotalElements(),
                participants.getTotalPages(),
                participants.isLast()
        );
    }

    private ClubEventCreateResponse toClubEventCreateResponse(ClubEvent event) {
        return ClubEventCreateResponse.builder()
                .id(event.getId())
                .slug(event.getSlug())
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
                .minLevel(event.getMinLevel())
                .maxLevel(event.getMaxLevel())
                .build();
    }

    private ClubEventResponse toClubEventResponse(ClubEvent event, Account account) {
        event = calculateStatus(event);
        Club club = event.getClub();
        ParticipantRoleEnum roleEnum= ParticipantRoleEnum.GUEST;
        if(account!=null) {
            if (club.getOwner().equals(account)){
                roleEnum = ParticipantRoleEnum.OWNER;
            }
            else if(clubMemberRepository.existsByClubAndAccountAndStatus(club, account, ClubMemberStatusEnum.APPROVED)){
                roleEnum = ParticipantRoleEnum.MEMBER;
            }
        }
        return ClubEventResponse.builder()
                .id(event.getId())
                .slug(event.getSlug())
                .image(fileStorageService.getFileUrl(event.getImage(), "/club/events"))
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .title(event.getTitle())
                .fee(event.getFee())
                .joinedMember((int) event.getParticipants().stream()
                        .filter(p -> !p.getStatus().equals(ClubEventParticipantStatusEnum.PENDING) )
                        .count())
                .totalMember(event.getTotalMember())
                .joinedOpenMembers((int) event.getParticipants().stream()
                        .filter(p -> !p.isClubMember() && !p.getStatus().equals(ClubEventParticipantStatusEnum.PENDING) )
                        .count())
                .maxOutsideMembers(event.getMaxOutsideMembers())
                .nameClub(event.getClub().getName())
                .categories(event.getCategories())
                .openForOutside(event.isOpenForOutside())
                .status(event.getStatus())
                .participantRole(roleEnum)
                .maxLevel(event.getMaxLevel())
                .minLevel(event.getMinLevel())
                .build();
    }

    private ClubEventDetailResponse toClubEventDetailResponse(ClubEvent event, Account account) {
        Club club = event.getClub();
        event = calculateStatus(event);
        ClubEventParticipant clubEventParticipant = clubEventParticipantRepository.findByClubEventAndParticipant(event,account).orElse(null);
        ParticipantRoleEnum roleEnum= ParticipantRoleEnum.GUEST;
        if(account!=null) {
            if (club.getOwner().equals(account)){
                roleEnum = ParticipantRoleEnum.OWNER;
            }
            else if(clubMemberRepository.existsByClubAndAccountAndStatus(club, account, ClubMemberStatusEnum.APPROVED)){
                roleEnum = ParticipantRoleEnum.MEMBER;
            }
        }
        return ClubEventDetailResponse.builder()
                .id(event.getId())
                .slug(event.getSlug())
                .title(event.getTitle())
                .description(event.getDescription())
                .requirements(event.getRequirements())
                .image(fileStorageService.getFileUrl(event.getImage(), "/club/events"))
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .totalMember(event.getTotalMember())
                .joinedMember((int) event.getParticipants().stream()
                        .filter(p -> !p.getStatus().equals(ClubEventParticipantStatusEnum.PENDING) )
                        .count())
                .categories(event.getCategories())
                .status(event.getStatus())
                .fee(event.getFee())
                .deadline(event.getDeadline())
                .openForOutside(event.isOpenForOutside())
                .maxClubMembers(event.getMaxClubMembers())
                .maxOutsideMembers(event.getMaxOutsideMembers())
                .joinedOpenMembers((int) event.getParticipants().stream()
                        .filter(p -> !p.isClubMember() && !p.getStatus().equals(ClubEventParticipantStatusEnum.PENDING) )
                        .count())
                .clubId(event.getClub().getId())
                .createdAt(event.getCreatedAt())
                .createdBy(event.getCreatedBy())
                .nameClub(event.getClub().getName())
                .isJoined(clubEventParticipantRepository.existsByClubEventAndParticipant(event, account))
                .participantRole(roleEnum)
                .maxLevel(event.getMaxLevel())
                .minLevel(event.getMinLevel())
                .participantStatus(clubEventParticipant != null ? clubEventParticipant.getStatus() : null)
                .isSendReason(absentReasonRepository.existsByParticipation(clubEventParticipant))
                .build();
    }
    public ClubEvent calculateStatus(ClubEvent event) {
        EventStatusEnum newStatus = event.getStatus();
        if(newStatus == EventStatusEnum.FINISHED){
            return event;
        }
        LocalDateTime now = LocalDateTime.now();
        if(event.getTotalMember() == event.getParticipants().size() && newStatus == EventStatusEnum.OPEN ){
            newStatus = EventStatusEnum.CLOSED;
        }
        if (now.isAfter(event.getDeadline()) && now.isBefore(event.getStartTime()) && newStatus == EventStatusEnum.OPEN ) {
            newStatus = EventStatusEnum.CLOSED;
        } else if (now.isAfter(event.getStartTime()) && now.isBefore(event.getEndTime())) {
            newStatus = EventStatusEnum.ONGOING;
        } else if (now.isAfter(event.getEndTime()) ) {
            newStatus = EventStatusEnum.FINISHED;
            clubService.calculateReputation(event.getClub());

        }

        // Nếu status thay đổi, lưu vào DB
        if (!event.getStatus().equals(newStatus)) {
            event.setStatus(newStatus);
            event = clubEventRepository.save(event);
        }
        return event;
    }


    public ClubEventDetailResponse updateClubEvent(ClubEventUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));

        ClubEvent clubEvent = clubEventRepository.findById(request.getId()).orElseThrow(() -> new InvalidDataException("Club not found"));

        clubEvent.setTitle(request.getTitle());
        clubEvent.setDescription(request.getDescription());
        clubEvent.setRequirements(request.getRequirements());
        if(request.getImage() != null) {
            clubEvent.setImage(request.getImage());
        }
        clubEvent.setLocation(request.getLocation());
        clubEvent.setStartTime(request.getStartTime());
        clubEvent.setEndTime(request.getEndTime());
        clubEvent.setDeadline(request.getDeadline());
        clubEvent.setOpenForOutside(request.isOpenForOutside());
        clubEvent.setFee(request.getFee());
        clubEvent.setDeadline(request.getDeadline());
        clubEvent.setCategories(request.getCategories());
        clubEvent.setMaxLevel(request.getMaxLevel());
        clubEvent.setMinLevel(request.getMinLevel());
        if(clubEvent.getStatus()!=request.getStatus() && request.getStatus() == EventStatusEnum.FINISHED){
            clubService.calculateReputation(clubEvent.getClub());
        }
        clubEvent.setStatus(request.getStatus());

        clubEvent = clubEventRepository.save(clubEvent);

        return toClubEventDetailResponse(clubEvent, account);
    }

    private boolean matchesQuickTimeFilter(ClubEvent event, String quickTimeFilter) {
        if (quickTimeFilter == null || quickTimeFilter.isEmpty()) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime eventStart = event.getStartTime();

        switch (quickTimeFilter) {
            case "Tuyển gấp":
                // Sự kiện có deadline trong vòng 24 giờ tới
                return event.getDeadline().isBefore(now.plusHours(24));

            case "Hôm nay":
                // Sự kiện diễn ra trong ngày hôm nay
                LocalDate today = now.toLocalDate();
                return eventStart.toLocalDate().equals(today);

            case "Cuối tuần":
                // Sự kiện diễn ra vào thứ 7 hoặc chủ nhật
                DayOfWeek dayOfWeek = eventStart.getDayOfWeek();
                return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

            case "Tuần này":
                // Sự kiện diễn ra trong tuần hiện tại
                LocalDate todayDate = now.toLocalDate();
                LocalDate startOfWeek = todayDate.with(DayOfWeek.MONDAY);
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                LocalDate eventDate = eventStart.toLocalDate();
                return !eventDate.isBefore(startOfWeek) && !eventDate.isAfter(endOfWeek);

            default:
                return true;
        }
    }

    private boolean matchesFeeFilter(ClubEvent event, Boolean isFree, BigDecimal minFee, BigDecimal maxFee) {
        // Lọc theo checkbox miễn phí
        if (isFree != null && isFree) {
            return event.getFee() == null || event.getFee().compareTo(BigDecimal.ZERO) == 0;
        }

        // Lọc theo range slider
        if (minFee != null && maxFee != null) {
            BigDecimal eventFee = event.getFee() != null ? event.getFee() : BigDecimal.ZERO;
            return eventFee.compareTo(minFee) >= 0 && eventFee.compareTo(maxFee) <= 0;
        }

        return true;
    }

    private boolean matchesDateRange(ClubEvent event, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }

        LocalDateTime eventStart = event.getStartTime();

        if (startDate != null && endDate != null) {
            return (eventStart.isAfter(startDate) || eventStart.isEqual(startDate))
                    && (eventStart.isBefore(endDate) || eventStart.isEqual(endDate));
        } else if (startDate != null) {
            return eventStart.isAfter(startDate) || eventStart.isEqual(startDate);
        } else {
            return eventStart.isBefore(endDate) || eventStart.isEqual(endDate);
        }
    }

    private boolean matchesSearch(ClubEvent event, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }

        String searchLower = search.toLowerCase();
        return event.getTitle().toLowerCase().contains(searchLower) ||
                event.getLocation().toLowerCase().contains(searchLower) ||
                (event.getClub() != null && event.getClub().getName().toLowerCase().contains(searchLower));
    }

    private boolean matchesProvince(ClubEvent event, String provinceFilter) {
        if (provinceFilter == null || provinceFilter.trim().isEmpty()) {
            return true;
        }

        String location = event.getLocation().toLowerCase();
        String provinceLower = provinceFilter.toLowerCase();

        return location.contains(provinceLower);
    }

    private boolean matchesWard(ClubEvent event, String wardFilter) {
        if (wardFilter == null || wardFilter.trim().isEmpty()) {
            return true;
        }

        String location = event.getLocation().toLowerCase();
        String wardLower = wardFilter.toLowerCase();

        // Logic đơn giản để tìm phường/xã trong location
        return location.contains(wardLower);
    }
}
