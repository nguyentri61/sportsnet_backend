package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.cancelEventReason.ClubEventCancellationResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubEventCancellationService {
    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final ClubEventCancellationRepository clubEventCancellationRepository;
    private final ClubEventRepository clubEventRepository;
    private final NotificationService notificationService;
    private final AccountRepository accountRepository;
    private final FileStorageService fileStorageService;
    private final ReputationHistoryRepository reputationHistoryRepository;

    /**
     * Duyệt hoặc từ chối yêu cầu hủy muộn
     */
    @Transactional
    public void reviewCancellation(String cancellationId, boolean approve) {
        ClubEventCancellation cancellation = clubEventCancellationRepository.findById(cancellationId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy yêu cầu hủy"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account reviewer = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản phê duyệt"));

        ClubEventParticipant participant = cancellation.getParticipant();
        ClubEvent event = participant.getClubEvent();
        Account requester = participant.getParticipant();

        // ✅ Chỉ chủ CLB có quyền duyệt
        if (!event.getClub().getOwner().equals(reviewer)) {
            throw new InvalidDataException("Chỉ chủ CLB mới có quyền phê duyệt yêu cầu này");
        }

        // ✅ Cập nhật thông tin phê duyệt
        cancellation.setApproved(approve);
        cancellation.setReviewedAt(Instant.now());
        cancellation.setReviewedBy(reviewer);
        clubEventCancellationRepository.save(cancellation);

        // ✅ Nếu được duyệt → chuyển trạng thái thành CANCELLED, KHÔNG trừ uy tín
        if (approve) {
            participant.setStatus(ClubEventParticipantStatusEnum.CANCELLED);
            clubEventParticipantRepository.save(participant);

            // Nếu trước đó từng trừ nhầm điểm (trong tương lai có cơ chế khác) thì cộng lại ở đây
            // nhưng hiện tại ta KHÔNG trừ tạm, nên bỏ qua phần cộng lại

            // 🔔 Gửi thông báo cho người yêu cầu
            notificationService.sendToAccount(
                    requester.getEmail(),
                    "Yêu cầu hủy tham gia đã được phê duyệt",
                    "Yêu cầu hủy của bạn cho hoạt động \"" + event.getTitle() + "\" đã được phê duyệt. Uy tín của bạn không bị ảnh hưởng.",
                    "/events/" + event.getSlug()
            );
        }
        // Nếu bị từ chối → vẫn coi là CANCELLED, nhưng trừ uy tín
        else {
            participant.setStatus(ClubEventParticipantStatusEnum.CANCELLED);
            clubEventParticipantRepository.save(participant);

            // Trừ điểm uy tín vì hủy muộn mà bị từ chối (ví dụ -10 điểm)
            int penaltyPoints = -10;
            requester.setReputationScore(Math.max(requester.getReputationScore() + penaltyPoints, 0));
            accountRepository.save(requester);

            // Lưu lịch sử uy tín
            ReputationHistory reputationHistory = ReputationHistory.builder()
                    .account(requester)
                    .change(penaltyPoints)
                    .reason("Hủy muộn hoạt động \"" + event.getTitle() + "\" nhưng bị từ chối phê duyệt.")
                    .build();
            reputationHistoryRepository.save(reputationHistory);

            // 🔔 Gửi thông báo cho người yêu cầu
            notificationService.sendToAccount(
                    requester.getEmail(),
                    "Yêu cầu hủy tham gia bị từ chối",
                    "Yêu cầu hủy của bạn cho hoạt động \"" + event.getTitle() + "\" đã bị từ chối. Bạn bị trừ 10 điểm uy tín.",
                    "/events/" + event.getSlug()
            );
        }
    }


    public List<ClubEventCancellationResponse> getCancellationsByEvent(String eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account requester = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));

        ClubEvent clubEvent = clubEventRepository.findById(eventId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy hoạt động"));

        // ✅ Phân quyền: chủ CLB hoặc có quyền quản lý
        Account owner = clubEvent.getClub().getOwner();
        if (!owner.getId().equals(requester.getId())) {
            throw new InvalidDataException("Bạn không có quyền xem danh sách hủy của hoạt động này");
        }

        // ✅ Lấy danh sách hủy (mới nhất trước)
        List<ClubEventCancellation> cancellations =
                clubEventCancellationRepository.findByParticipant_ClubEventOrderByRequestedAtDesc(clubEvent);

        return cancellations.stream()
                .map(c -> ClubEventCancellationResponse.builder()
                        .cancellationId(c.getId())
                        .participantId(c.getParticipant().getParticipant().getId())
                        .accountSlug(c.getParticipant().getParticipant().getUserInfo().getSlug())
                        .avatarUrl(fileStorageService.getFileUrl(
                                c.getParticipant().getParticipant().getUserInfo().getAvatarUrl(), "/avatar"))
                        .fullName(c.getParticipant().getParticipant().getUserInfo().getFullName())
                        .email(c.getParticipant().getParticipant().getEmail())
                        .reason(c.getReason())
                        .approved(c.getApproved())
                        .lateCancellation(c.getLateCancellation() != null ? c.getLateCancellation() : null) // ✅ hiển thị hủy sát giờ
                        .cancelDate(c.getRequestedAt())
                        .reviewedAt(c.getReviewedAt())
                        .reviewedBy(c.getReviewedBy() != null
                                ? c.getReviewedBy().getUserInfo().getFullName()
                                : null)
                        .build())
                .toList();
    }

}
