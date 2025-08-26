package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.ClubEventCreateRequest;
import com.tlcn.sportsnet_backend.dto.club_event.ClubEventResponse;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.ClubEventRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ClubEventService {
    private final ClubRepository clubRepository;
    private final ClubEventRepository clubEventRepository;
    private final FileStorageService fileStorageService;

    public ClubEventResponse createClubEvent(ClubEventCreateRequest request) {
        Club club = clubRepository.findById(request.getClubId())
                .orElseThrow(() -> new InvalidDataException("Club not found"));

        ClubEvent event = ClubEvent.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .image(request.getImage())
                .date(request.getDate())
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
                .build();

        clubEventRepository.save(event);

        return toClubEventResponse(event);
    }

    public ClubEventResponse getEventClubInfo(String id) {
        ClubEvent event = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Club not found"));
        return toClubEventResponse(event);
    }

    private ClubEventResponse toClubEventResponse(ClubEvent event) {
        return ClubEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
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


}
