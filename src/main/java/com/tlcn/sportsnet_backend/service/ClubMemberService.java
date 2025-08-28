package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClubMemberService {
    private final ClubRepository clubRepository;
    private final AccountRepository accountRepository;
    private final ClubMemberRepository clubMemberRepository;

    public ClubMember joinClub(String clubId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new InvalidDataException("Club not found"));

        if (clubMemberRepository.existsByClubAndAccount(club, account)) {
            throw new InvalidDataException("User already joined this club");
        }

        ClubMember clubMember = ClubMember.builder()
                .club(club)
                .account(account)
                .role(ClubMemberRoleEnum.MEMBER)
                .status(ClubMemberStatusEnum.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();

        return clubMemberRepository.save(clubMember);
    }

    // Owner duyệt/Reject
    public void approveMember(String clubId, String memberId, boolean approve) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account owner = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new InvalidDataException("Club not found"));

        // Kiểm tra quyền: owner mới có quyền duyệt
        if (!club.getOwner().getId().equals(owner.getId())) {
            throw new InvalidDataException("Not club owner");
        }

        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidDataException("Member not found"));

        if (!member.getClub().getId().equals(club.getId())) {
            throw new InvalidDataException("Member does not belong to this club");
        }

        if (approve) {
            member.setStatus(ClubMemberStatusEnum.APPROVED);
            clubMemberRepository.save(member);
        } else {
            // Reject thì xóa record luôn
            clubMemberRepository.delete(member);
        }
    }

    // Ban 1 thành viên
    public void banMember(String clubId, String memberId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account owner = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new InvalidDataException("Club not found"));

        if (!club.getOwner().getId().equals(owner.getId())) {
            throw new InvalidDataException("Not club owner");
        }

        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidDataException("Member not found"));

        if (!member.getClub().getId().equals(club.getId())) {
            throw new InvalidDataException("Member does not belong to this club");
        }

        member.setStatus(ClubMemberStatusEnum.BANNED);
        clubMemberRepository.save(member);
    }

}
