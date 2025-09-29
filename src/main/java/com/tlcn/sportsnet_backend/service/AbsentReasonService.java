package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.absentReason.AbsentReasonRequest;
import com.tlcn.sportsnet_backend.dto.absentReason.AbsentReasonResponse;
import com.tlcn.sportsnet_backend.entity.AbsentReason;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.entity.ReputationHistory;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.enums.RequestStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AbsentReasonRepository;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventParticipantRepository;
import com.tlcn.sportsnet_backend.repository.ReputationHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AbsentReasonService {

    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final AbsentReasonRepository absentReasonRepository;
    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final ReputationHistoryRepository reputationHistoryRepository;
    public AbsentReasonResponse createAbsentReason(AbsentReasonRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEventParticipant clubEventParticipant = clubEventParticipantRepository.findByClubEvent_IdAndParticipant(request.getIdEvent(), account).orElseThrow(() -> new InvalidDataException("Event participant not found"));
        if(clubEventParticipant.getStatus()!= ClubEventParticipantStatusEnum.ABSENT){
            throw new InvalidDataException("Event participant status is not ABSENT");
        }
        AbsentReason absentReason = AbsentReason.builder()
                .reason(request.getReason())
                .participation(clubEventParticipant)
                .build();
        absentReason = absentReasonRepository.save(absentReason);
        notificationService.sendToAccount(clubEventParticipant.getClubEvent().getClub().getOwner(),"Lý do vắng "+clubEventParticipant.getClubEvent().getTitle() ,"Người dùng "+ clubEventParticipant.getParticipant().getUserInfo().getFullName()+" đã gửi lý vắng mặt","/events/"+clubEventParticipant.getClubEvent().getSlug());
        return toAbsentReason(absentReason);

    }

    @Transactional
    public String approveAbsentReason(String id) {
        AbsentReason absentReason = checkPermission(id);
        Account account = absentReason.getParticipation().getParticipant();
        account.setReputationScore(account.getReputationScore()+20);
        absentReason.setStatus(RequestStatusEnum.APPROVED);
        absentReason.setReviewedAt(Instant.now());
        ReputationHistory reputationHistory = ReputationHistory.builder()
                .reason("Khôi phục điểm vì vắng có ly do ở hoạt động "+ absentReason.getParticipation().getClubEvent().getTitle())
                .change(20)
                .account(account)
                .build();
        accountRepository.save(account);
        absentReasonRepository.save(absentReason);
        reputationHistoryRepository.save(reputationHistory);
        notificationService.sendToAccount(account,"Phê duyệt lý do vắng mặt " ,"Lý do vắng mặt tại hoạt động "+ absentReason.getParticipation().getClubEvent().getTitle() +" đã được phê duyệt và khôi phụ điểm uy tín","/profile");
        return "Đã chấp nhận lý do vắng";
    }

    @Transactional
    public String rejectAbsentReason(String id) {
        AbsentReason absentReason = checkPermission(id);
        absentReason.setStatus(RequestStatusEnum.REJECTED);
        absentReason.setReviewedAt(Instant.now());

        absentReasonRepository.save(absentReason);

        return "Đã từ chối lý do vắng";
    }
    public AbsentReasonResponse getByIdPart(String idPart) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEventParticipant clubEventParticipant = clubEventParticipantRepository.findById(idPart).orElseThrow(() -> new InvalidDataException("Club event participant not found"));
        if(clubEventParticipant.getStatus()!= ClubEventParticipantStatusEnum.ABSENT){
            throw new InvalidDataException("Event participant status is not ABSENT");
        }
        if(clubEventParticipant.getClubEvent().getClub().getOwner()!=account){
            throw new InvalidDataException("You don't have permission");
        }
        AbsentReason absentReason = absentReasonRepository.findByParticipation_Id(idPart).orElseThrow(() -> new InvalidDataException("AbsentReason not found"));
        return toAbsentReason(absentReason);
    }

    public AbsentReasonResponse toAbsentReason(AbsentReason absentReason) {
        return AbsentReasonResponse.builder()
                .id(absentReason.getId())
                .reason(absentReason.getReason())
                .reviewedAt(absentReason.getReviewedAt())
                .createdAt(absentReason.getCreatedAt())
                .status(absentReason.getStatus())
                .build();
    }

    public AbsentReason checkPermission(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        AbsentReason absentReason = absentReasonRepository.findById(id).orElseThrow(() -> new InvalidDataException("AbsentReason not found"));
        if(absentReason.getParticipation().getClubEvent().getClub().getOwner()!=account){
            throw new InvalidDataException("You don't have permission");
        }
        return absentReason;
    }



}
