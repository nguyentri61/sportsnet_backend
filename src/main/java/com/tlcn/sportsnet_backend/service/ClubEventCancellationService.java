package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventCancellation;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventCancellationRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClubEventCancellationService {
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final ClubEventCancellationRepository clubEventCancellationRepository;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;

    /**
     * Gửi yêu cầu hủy muộn
     */
    public void requestLateCancel(String participantId, String reason) {
        ClubEventParticipant participant = clubEventParticipantRepository.findById(participantId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy bản ghi tham gia hoạt động"));

        // kiểm tra quyền
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account current = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        if (!participant.getParticipant().equals(current)) {
            throw new InvalidDataException("Bạn không có quyền hủy bản ghi này");
        }

        // kiểm tra trạng thái
        if (participant.getStatus() == ClubEventParticipantStatusEnum.CANCELLED) {
            throw new InvalidDataException("Bạn đã hủy tham gia hoạt động này rồi");
        }

        ClubEvent event = participant.getClubEvent();
        if (LocalDateTime.now().isBefore(event.getDeadline())) {
            throw new InvalidDataException("Chỉ có thể gửi yêu cầu hủy sau hạn chót");
        }

        // tạo yêu cầu mới
        ClubEventCancellation cancellation = ClubEventCancellation.builder()
                .participant(participant)
                .reason(reason)
                .approved(null)
                .build();

        clubEventCancellationRepository.save(cancellation);

        // cập nhật participant sang trạng thái chờ phê duyệt
        participant.setStatus(ClubEventParticipantStatusEnum.CANCELLATION_PENDING);
        clubEventParticipantRepository.save(participant);

        // gửi thông báo cho chủ CLB
        Account owner = event.getClub().getOwner();
        notificationService.sendToAccount(
                owner,
                "Yêu cầu phê duyệt hủy tham gia",
                participant.getParticipant().getUserInfo().getFullName() +
                        " đã gửi yêu cầu hủy tham gia hoạt động: " + event.getTitle(),
                "/events/" + event.getSlug()
        );
    }

    /**
     * Duyệt hoặc từ chối yêu cầu hủy muộn
     */
    public void reviewCancellation(String cancellationId, boolean approve) {
        ClubEventCancellation cancellation = clubEventCancellationRepository.findById(cancellationId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy yêu cầu hủy"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account reviewer = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEventParticipant participant = cancellation.getParticipant();
        ClubEvent event = participant.getClubEvent();

        // chỉ chủ CLB có quyền duyệt
        if (!event.getClub().getOwner().equals(reviewer)) {
            throw new InvalidDataException("Chỉ chủ CLB mới có quyền phê duyệt yêu cầu này");
        }

        cancellation.setApproved(approve);
        cancellation.setReviewedAt(java.time.Instant.now());
        cancellation.setReviewedBy(reviewer);
        clubEventCancellationRepository.save(cancellation);

        if (approve) {
            participant.setStatus(ClubEventParticipantStatusEnum.CANCELLED);
        } else {
            participant.setStatus(ClubEventParticipantStatusEnum.CANCELLED);
            participant.setDeductedForAbsent(true); // trừ điểm uy tín
        }
        clubEventParticipantRepository.save(participant);

        // gửi thông báo cho người gửi
        notificationService.sendToAccount(
                participant.getParticipant(),
                "Kết quả phê duyệt hủy tham gia",
                "Yêu cầu hủy của bạn cho hoạt động " + event.getTitle() +
                        (approve ? " đã được phê duyệt." : " đã bị từ chối và bạn bị trừ điểm uy tín."),
                "/events/" + event.getSlug()
        );
    }
}
