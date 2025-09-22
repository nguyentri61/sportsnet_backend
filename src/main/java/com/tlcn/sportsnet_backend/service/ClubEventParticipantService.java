package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.ClubEventResponse;
import com.tlcn.sportsnet_backend.dto.club_event_participant.ClubEventParticipantResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubEventParticipantService {
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final PlayerRatingRepository playerRatingRepository;
    private final NotificationService notificationService;
    public ClubEventParticipantResponse joinClubEvent(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        ClubEvent clubEvent = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));
        Club club = clubEvent.getClub();
        int totalOpenJoinedMembers = (int) clubEvent.getParticipants().stream()
                .filter(p -> !p.isClubMember() )
                .count();

        if(clubEventParticipantRepository.existsByClubEventAndParticipant(clubEvent, account)){
            throw new InvalidDataException("Bạn đã tham gia hoạt động rồi.");
        }
        boolean isMember = clubMemberRepository.existsByClubAndAccountAndStatus(club, account, ClubMemberStatusEnum.APPROVED);
        if (!isMember && !clubEvent.isOpenForOutside() ) {
            throw new InvalidDataException("Hoạt động chỉ dành cho người trong CLB");
        }
        if (!isMember && totalOpenJoinedMembers >= clubEvent.getMaxOutsideMembers() ) {
            throw new InvalidDataException("Đã đủ số lượng vãng lai");
        }
        if(LocalDateTime.now().isAfter(clubEvent.getDeadline()) || clubEvent.getStatus() != EventStatusEnum.OPEN){
            throw new InvalidDataException("Hoạt động đã hết hạn");
        }
        ClubEventParticipant clubEventParticipant = ClubEventParticipant.builder()
                .clubEvent(clubEvent)
                .participant(account)
                .isClubMember(isMember)
                .status(isMember ? ClubEventParticipantStatusEnum.APPROVED : ClubEventParticipantStatusEnum.PENDING)
                .build();
        clubEventParticipant = clubEventParticipantRepository.save(clubEventParticipant);
        String message;
        if(clubEventParticipant.isClubMember()){
            message = clubEventParticipant.getParticipant().getUserInfo().getFullName() +", thành viên CLB đã tham gia hoạt động";
        }
        else {
            message = clubEventParticipant.getParticipant().getUserInfo().getFullName() +" đã đăng ký tham gia hoạt động, vui lòng phê duyệt";
        }
        notificationService.sendToAccount(club.getOwner(),"Hoạt động: "+clubEvent.getTitle() ,message,"/events/"+clubEvent.getSlug());
        return toParticipantResponse(clubEventParticipant);

    }

    public List<ClubEventParticipantResponse> getAllParticipantClubEvent(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEvent clubEvent = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Event not found"));
        if(!account.equals(clubEvent.getClub().getOwner())){
            throw new InvalidDataException("You are not allowed to view participants");
        }
        List<ClubEventParticipant> clubEventParticipants = clubEventParticipantRepository.findAllByClubEventOrderByJoinedAtDesc(clubEvent);
        return  clubEventParticipants.stream()
                .map(this::toParticipantResponse)
                .toList();


    }

    public ClubEventParticipantResponse toParticipantResponse(ClubEventParticipant clubEventParticipant) {
        PlayerRating playerRating = playerRatingRepository.findByAccount(clubEventParticipant.getParticipant()).orElse(null);
        return ClubEventParticipantResponse.builder()
                .id(clubEventParticipant.getId())
                .isClubMember(clubEventParticipant.isClubMember())
                .email(clubEventParticipant.getParticipant().getEmail())
                .fullName(clubEventParticipant.getParticipant().getUserInfo().getFullName())
                .gender(clubEventParticipant.getParticipant().getUserInfo().getGender())
                .avatarUrl(fileStorageService.getFileUrl(clubEventParticipant.getParticipant().getUserInfo().getAvatarUrl(), "/avatar") )
                .joinedAt(clubEventParticipant.getJoinedAt())
                .status(clubEventParticipant.getStatus())
                .experience(playerRating != null ? playerRating.getExperience() : null)
                .averageTechnicalScore(playerRating != null ? playerRating.getAverageTechnicalScore() : null)
                .tactics(playerRating != null ? playerRating.getTactics() : null)
                .stamina(playerRating != null ? playerRating.getStamina() : null)
                .overallScore(playerRating != null ? playerRating.getOverallScore() : null)
                .skillLevel(playerRating != null ? playerRating.getSkillLevel() : "")
                .slug(clubEventParticipant.getParticipant().getUserInfo().getSlug())
                .build();

    }


    public String approveParticipant(String id, String eventId) {
        ClubEvent clubEvent = checkPermission(eventId);
        ClubEventParticipant clubMember = clubEventParticipantRepository.findById(id).orElseThrow(() -> new InvalidDataException("Member not found"));
        clubMember.setStatus(ClubEventParticipantStatusEnum.APPROVED);
        clubEventParticipantRepository.save(clubMember);
        notificationService.sendToAccount(clubMember.getParticipant(),"Hoạt động: "+clubEvent.getTitle() ,"Bạn đã được phê duyệt tham gia hoạt động "+ clubEvent.getTitle(),"/events/"+clubEvent.getSlug());
        return "Đã chấp nhận người tham gia";
    }

    public String rejectParticipant(String id, String idEvent) {
        ClubEvent clubEvent = checkPermission(idEvent);
        ClubEventParticipant clubMember = clubEventParticipantRepository.findById(id).orElseThrow(() -> new InvalidDataException("Member not found"));
        clubEventParticipantRepository.delete(clubMember);
        notificationService.sendToAccount(clubMember.getParticipant(),"Hoạt động: "+clubEvent.getTitle() ,"Vì 1 số lý do bạn đã bị từ chối tham gia hoạt động "+ clubEvent.getTitle(),"/events/"+clubEvent.getSlug());
        return "Đã từ chối người tham gia";
    }

    public ClubEvent checkPermission(String idEvent) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEvent clubEvent = clubEventRepository.findById(idEvent).orElseThrow(() -> new InvalidDataException("Event not found"));
        if(!account.equals(clubEvent.getClub().getOwner())){
            throw new InvalidDataException("You are not allowed to view participants");
        }
        return clubEvent;
    }
}
