package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.club_event_participant.ClubEventParticipantResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventParticipantRepository;
import com.tlcn.sportsnet_backend.repository.ClubEventRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberRepository;
import lombok.RequiredArgsConstructor;
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
    public ClubEventParticipantResponse joinClub(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubEvent clubEvent = clubEventRepository.findById(id).orElseThrow(() -> new InvalidDataException("Event not found"));
        Club club = clubEvent.getClub();
        if(clubEventParticipantRepository.existsByClubEventAndParticipant(clubEvent, account)){
            throw new InvalidDataException("You already join this event");
        }
        boolean isMember = clubMemberRepository.existsByClubAndAccount(club, account);
        if (!isMember && !clubEvent.isOpenForOutside() ) {
            throw new InvalidDataException("You are not allowed to join this event");
        }
        if(LocalDateTime.now().isAfter(clubEvent.getDeadline()) || clubEvent.getStatus() != EventStatusEnum.OPEN){
            throw new InvalidDataException("You are not allowed to join this event");
        }
        ClubEventParticipant clubEventParticipant = ClubEventParticipant.builder()
                .clubEvent(clubEvent)
                .participant(account)
                .isClubMember(isMember)
                .build();
        clubEventParticipant = clubEventParticipantRepository.save(clubEventParticipant);
        return toParticipantResponse(clubEventParticipant);

    }

    public ClubEventParticipantResponse toParticipantResponse(ClubEventParticipant clubEventParticipant) {
        return ClubEventParticipantResponse.builder()
                .id(clubEventParticipant.getId())
                .isClubMember(clubEventParticipant.isClubMember())
                .email(clubEventParticipant.getParticipant().getEmail())
                .fullName(clubEventParticipant.getParticipant().getUserInfo().getFullName())
                .gender(clubEventParticipant.getParticipant().getUserInfo().getGender())
                .avatarUrl(clubEventParticipant.getParticipant().getUserInfo().getAvatarUrl())
                .joinedAt(clubEventParticipant.getJoinedAt())
                .build();

    }
}
