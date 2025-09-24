package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.absentReason.AbsentReasonRequest;
import com.tlcn.sportsnet_backend.dto.absentReason.AbsentReasonResponse;
import com.tlcn.sportsnet_backend.entity.AbsentReason;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import com.tlcn.sportsnet_backend.enums.ClubEventParticipantStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AbsentReasonRepository;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbsentReasonService {

    private final ClubEventParticipantRepository clubEventParticipantRepository;
    private final AbsentReasonRepository absentReasonRepository;
    private final AccountRepository accountRepository;
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
}
