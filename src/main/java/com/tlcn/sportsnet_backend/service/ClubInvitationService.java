package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.clubInvitation.ClubInvitationRequest;
import com.tlcn.sportsnet_backend.dto.clubInvitation.ClubInvitationResponse;
import com.tlcn.sportsnet_backend.dto.clubInvitation.ClubInvitationUpdateStatus;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubInvitation;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubInvitationRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubInvitationService {

    private final AccountRepository accountRepository;
    private final ClubRepository clubRepository;
    private final ClubInvitationRepository clubInvitationRepository;
    private final NotificationService notificationService;
    private final ClubMemberRepository clubMemberRepository;
    public Object createClubInvitation(ClubInvitationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        Account receiver = accountRepository.findById(request.getReceiverId()).orElseThrow(() -> new InvalidDataException("Receiver not found"));
        Club club = clubRepository.findById(request.getClubId()).orElseThrow(() -> new InvalidDataException("Club not found"));
        if(!club.getOwner().getId().equals(account.getId())) {
            throw new InvalidDataException("Club owner already exists");
        }
        ClubInvitation clubInvitation = ClubInvitation.builder()
                .message(request.getMessage())
                .club(club)
                .receiver(receiver)
                .build();
        clubInvitation = clubInvitationRepository.save(clubInvitation);
        notificationService.sendToAccount(receiver,"Lời mời tham gia CLB", "Bạn nhận được lời mời tham gia vào CLB "+club.getName(), "/clubs/"+ club.getSlug());
        return toClubInvitation(clubInvitation);
    }

    public Object updateStatusClubInvitation(ClubInvitationUpdateStatus request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        ClubInvitation clubInvitation = clubInvitationRepository.findById(request.getId()).orElseThrow(() -> new InvalidDataException("ClubInvitation not found"));
        if(!clubInvitation.getReceiver().getId().equals(account.getId()) && !clubInvitation.getClub().getOwner().getId().equals(account.getId())) {
            throw new InvalidDataException("Người dùng truy cập không hợp lệ");
        }
        clubInvitation.setStatus(request.getStatus());
        clubInvitationRepository.save(clubInvitation);
        if(request.getStatus().equals(InvitationStatusEnum.ACCEPTED)){
            ClubMember clubMember = ClubMember.builder()
                    .club(clubInvitation.getClub())
                    .account(account)
                    .role(ClubMemberRoleEnum.MEMBER)
                    .status(ClubMemberStatusEnum.APPROVED)
                    .joinedAt(Instant.now())
                    .build();
            clubMemberRepository.save(clubMember);
            notificationService.sendToAccount(clubInvitation.getClub().getOwner(), "Đồng ý tham gia CLB", account.getUserInfo().getFullName()+" đã đồng ý tham gia vào CLB "+ clubInvitation.getClub().getName() + " của bạn", "/my-clubs/"+ clubInvitation.getClub().getSlug());
        }
        else if(request.getStatus().equals(InvitationStatusEnum.REJECTED)){
            notificationService.sendToAccount(clubInvitation.getClub().getOwner(), "Từ chối tham gia CLB",account.getUserInfo().getFullName()+" đã từ chối tham gia vào CLB "+ clubInvitation.getClub().getName() + " của bạn", "/my-clubs/"+ clubInvitation.getClub().getSlug());
        }
        return toClubInvitation(clubInvitation);
    }
    public ClubInvitationResponse toClubInvitation(ClubInvitation clubInvitation) {
        return ClubInvitationResponse.builder()
                .id(clubInvitation.getId())
                .message(clubInvitation.getMessage())
                .clubId(clubInvitation.getClub().getId())
                .clubName(clubInvitation.getClub().getName())
                .receiverId(clubInvitation.getReceiver().getId())
                .receiverName(clubInvitation.getReceiver().getUserInfo().getFullName())
                .respondedAt(clubInvitation.getRespondedAt())
                .sendAt(clubInvitation.getSentAt())
                .status(clubInvitation.getStatus())
                .build();
    }


    public Object getClubByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        List<ClubInvitation> list = clubInvitationRepository.findAllByReceiver_id(account.getId());
        List<ClubInvitationResponse> responses = new ArrayList<>();
        for(ClubInvitation clubInvitation : list) {
            responses.add(toClubInvitation(clubInvitation));
        }
        return responses;
    }
}
