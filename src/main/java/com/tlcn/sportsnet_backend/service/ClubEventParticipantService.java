package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event.ClubEventResponse;
import com.tlcn.sportsnet_backend.dto.club_event_participant.ClubEventParticipantResponse;
import com.tlcn.sportsnet_backend.dto.club_event_participant.ClubEventParticipantUpdate;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.*;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClubEventParticipantService {
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final ClubEventRepository clubEventRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final PlayerRatingRepository playerRatingRepository;
    private final ReputationHistoryRepository reputationHistoryRepository;
    private final NotificationService notificationService;
    private final AbsentReasonRepository absentReasonRepository;
    private final UserScheduleService userScheduleService;
    private final UserScheduleRepository userScheduleRepository;
    private final ClubEventCancellationRepository clubEventCancellationRepository;


    public ClubEventParticipantResponse joinClubEvent(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        ClubEvent clubEvent = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));
        Club club = clubEvent.getClub();

        // Chỉ đếm những người vãng lai (không phải thành viên CLB) và có trạng thái KHÁC CANCELLED
        int totalOpenJoinedMembers = (int) clubEvent.getParticipants().stream()
                .filter(p -> !p.isClubMember() && p.getStatus() != ClubEventParticipantStatusEnum.CANCELLED )
                .count();

        // Kiểm tra nếu người dùng đã có bản ghi tham gia nhưng chưa hủy thì không cho join lại
        if(clubEventParticipantRepository.existsByClubEventAndParticipantAndStatusNot(clubEvent, account, ClubEventParticipantStatusEnum.CANCELLED)){
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
        ClubEventParticipant clubEventParticipant = clubEventParticipantRepository
                .findByClubEventAndParticipant(clubEvent, account)
                .map(existing -> {
                    existing.setStatus(isMember ? ClubEventParticipantStatusEnum.APPROVED : ClubEventParticipantStatusEnum.PENDING);
                    return existing;
                })
                .orElseGet(() -> ClubEventParticipant.builder()
                        .clubEvent(clubEvent)
                        .participant(account)
                        .isClubMember(isMember)
                        .status(isMember ? ClubEventParticipantStatusEnum.APPROVED : ClubEventParticipantStatusEnum.PENDING)
                        .build());

        clubEventParticipant = clubEventParticipantRepository.save(clubEventParticipant);
        String message;
        if(clubEventParticipant.isClubMember()){
            message = clubEventParticipant.getParticipant().getUserInfo().getFullName() +", thành viên CLB đã tham gia hoạt động";
        }
        else {
            message = clubEventParticipant.getParticipant().getUserInfo().getFullName() +" đã đăng ký tham gia hoạt động, vui lòng phê duyệt";
        }

        // Xử lý UserSchedule tránh tao moi trùng
        Optional<UserSchedule> existingScheduleOpt = userScheduleRepository.findByAccountAndClubEvent(account, clubEvent);
        if (existingScheduleOpt.isPresent()) {
            UserSchedule existingSchedule = existingScheduleOpt.get();
            if (existingSchedule.getStatus() == StatusScheduleEnum.CANCELLED) {
                existingSchedule.setStatus(clubEventParticipant.getStatus().toStatusEnum());
                userScheduleRepository.save(existingSchedule);
            }
        } else {
            userScheduleService.createScheduleByClubEvent(clubEvent, clubEventParticipant);
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
                .totalParticipatedEvents(clubEventParticipant.getParticipant().getTotalParticipatedEvents())
                .reputationScore(clubEventParticipant.getParticipant().getReputationScore())
                .isSendReason(absentReasonRepository.existsByParticipation(clubEventParticipant))
                .build();

    }

    @Transactional
    public String cancelJoinEvent(String eventId, String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));

        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidDataException("Vui lòng nhập lý do hủy tham gia hoạt động");
        }

        ClubEvent clubEvent = clubEventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));

        ClubEventParticipant participant = clubEventParticipantRepository
                .findByClubEventAndParticipant(clubEvent, account)
                .orElseThrow(() -> new InvalidDataException("Bạn chưa tham gia hoạt động này"));

        if (participant.getStatus() == ClubEventParticipantStatusEnum.CANCELLED) {
            throw new InvalidDataException("Bạn đã hủy tham gia trước đó");
        }

        LocalDateTime now = LocalDateTime.now();

        // Không thể hủy khi hoạt động đã bắt đầu
        if (now.isAfter(clubEvent.getStartTime())) {
            throw new InvalidDataException("Không thể hủy khi hoạt động đã bắt đầu");
        }

        // Xác định hủy sát giờ (< 24h trước khi bắt đầu)
        boolean isLateCancellation = now.isAfter(clubEvent.getStartTime().minusHours(24));

        // Trường hợp hủy sớm (trước deadline và không sát giờ)
        if (now.isBefore(clubEvent.getDeadline()) && !isLateCancellation) {
            participant.setStatus(ClubEventParticipantStatusEnum.CANCELLED);
            clubEventParticipantRepository.save(participant);

            // Lưu lịch sử hủy
            ClubEventCancellation cancellation = ClubEventCancellation.builder()
                    .participant(participant)
                    .reason(reason)
                    .approved(true) // hủy hợp lệ, không cần duyệt
                    .lateCancellation(false)
                    .requestedAt(Instant.now())
                    .reviewedAt(Instant.now())
                    .reviewedBy(clubEvent.getClub().getOwner())
                    .build();
            clubEventCancellationRepository.save(cancellation);

            // Gửi thông báo cho chủ CLB
            String message = account.getUserInfo().getFullName()
                    + " đã hủy tham gia hoạt động " + clubEvent.getTitle()
                    + " (Lý do: " + reason + ")";
            notificationService.sendToAccount(
                    clubEvent.getClub().getOwner(),
                    "Hoạt động: " + clubEvent.getTitle(),
                    message,
                    "/events/" + clubEvent.getSlug()
            );

            return "Đã hủy tham gia hoạt động thành công.";
        }

        // Trường hợp hủy muộn (sau deadline hoặc sát giờ)
        participant.setStatus(ClubEventParticipantStatusEnum.CANCELLATION_PENDING);
        clubEventParticipantRepository.save(participant);

        // Tạo yêu cầu phê duyệt
        ClubEventCancellation cancellation = ClubEventCancellation.builder()
                .participant(participant)
                .reason(reason)
                .approved(null) // chờ duyệt
                .lateCancellation(true)
                .requestedAt(Instant.now())
                .build();
        clubEventCancellationRepository.save(cancellation);

        // 🔔 Gửi thông báo cho chủ CLB
        String message = account.getUserInfo().getFullName()
                + " đã gửi yêu cầu hủy tham gia hoạt động " + clubEvent.getTitle()
                + " (hủy muộn, chờ phê duyệt). Lý do: " + reason;
        notificationService.sendToAccount(
                clubEvent.getClub().getOwner(),
                "Yêu cầu phê duyệt hủy tham gia",
                message,
                "/events/" + clubEvent.getSlug()
        );

        return "Yêu cầu hủy của bạn đã được gửi đến ban quản lý CLB để phê duyệt (do hủy muộn).";
    }


    public String approveParticipant(String id, String eventId) {
        ClubEvent clubEvent = checkPermission(eventId);
        ClubEventParticipant clubMember = clubEventParticipantRepository.findById(id).orElseThrow(() -> new InvalidDataException("Member not found"));
        clubMember.setStatus(ClubEventParticipantStatusEnum.APPROVED);
        clubEventParticipantRepository.save(clubMember);
        notificationService.sendToAccount(clubMember.getParticipant(),"Hoạt động: "+clubEvent.getTitle() ,"Bạn đã được phê duyệt tham gia hoạt động "+ clubEvent.getTitle(),"/events/"+clubEvent.getSlug());
        return "Đã chấp nhận người tham gia";
    }

    public String rejectParticipant(String id, String idEvent, String reason) {
        ClubEvent clubEvent = checkPermission(idEvent);
        ClubEventParticipant clubMember = clubEventParticipantRepository.findById(id).orElseThrow(() -> new InvalidDataException("Member not found"));
        UserSchedule userSchedule = userScheduleRepository.findByAccountIdAndClubEventId(clubMember.getParticipant().getId(), idEvent);
        userScheduleRepository.delete(userSchedule);
        clubEventParticipantRepository.delete(clubMember);
        String message = "Vì 1 số lý do bạn đã bị từ chối tham gia hoạt động "+ clubEvent.getTitle();
        if(!reason.isEmpty()){
            message = reason;
        }
        notificationService.sendToAccount(clubMember.getParticipant(),"Hoạt động: "+clubEvent.getTitle() ,message,"/events/"+clubEvent.getSlug());
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

    public String updateParticipant(String id, String idEvent, ClubEventParticipantUpdate status) {
        ClubEvent clubEvent = checkPermission(idEvent);
        ClubEventParticipant clubMember = clubEventParticipantRepository.findById(id).orElseThrow(() -> new InvalidDataException("Member not found"));
        clubMember.setStatus(status.getStatus());
        clubEventParticipantRepository.save(clubMember);
        UserSchedule userSchedule = userScheduleRepository.findByAccountIdAndClubEventId(clubMember.getParticipant().getId(), idEvent);
        userSchedule.setStatus(status.getStatus().toStatusEnum());
        userScheduleRepository.save(userSchedule);
        Account account = clubMember.getParticipant();
        String content;
        if(status.getStatus() == ClubEventParticipantStatusEnum.ATTENDED){
            content = "Bạn đã được đánh giá là đã tham gia hoạt động, bạn được cộng 10 điểm uy tín ";
            account.setTotalParticipatedEvents(account.getTotalParticipatedEvents() + 1);
            account.setReputationScore(Math.min(account.getReputationScore() + 10, 100));
            ReputationHistory reputationHistory = ReputationHistory.builder()
                    .account(account)
                    .change(10)
                    .reason("Đăng ký và tham gia hoạt động "+ clubEvent.getTitle())
                    .build();
            accountRepository.save(account);
            reputationHistoryRepository.save(reputationHistory);
        }
        else {
            content = "Bạn đã bị đánh giá là không tham gia hoạt động, bạn bị trừ 20 điểm uy tín";
            account.setReputationScore(account.getReputationScore() - 20);
            ReputationHistory reputationHistory = ReputationHistory.builder()
                    .account(account)
                    .change(-20)
                    .reason("Đăng ký nhưng không tham gia hoạt động "+ clubEvent.getTitle())
                    .build();
            accountRepository.save(account);
            reputationHistoryRepository.save(reputationHistory);

        }
        notificationService.sendToAccount(clubMember.getParticipant(),"Hoạt động: "+clubEvent.getTitle() ,content,"/events/"+clubEvent.getSlug());
        return "Đã xác nhân người tham gia";
    }

    public boolean canJoin(String eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEvent event = clubEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        // Nếu sự kiện đã kết thúc
        if (event.getEndTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        // Kiểm tra có bị trùng lịch không
        boolean conflict = userScheduleRepository.hasConflictWithEventTime(
                account.getId(),
                event.getStartTime(),
                event.getEndTime()
        );

        return !conflict;
    }
}
