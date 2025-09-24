package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.member.DetailMemberResponse;
import com.tlcn.sportsnet_backend.dto.member.MemberResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.entity.PlayerRating;
import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ClubMemberRepository;
import com.tlcn.sportsnet_backend.repository.ClubRepository;
import com.tlcn.sportsnet_backend.repository.PlayerRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMemberService {
    private final ClubRepository clubRepository;
    private final AccountRepository accountRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final PlayerRatingRepository playerRatingRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    
    public String joinClub(String clubId, String notification) {
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
                .note(notification)
                .role(ClubMemberRoleEnum.MEMBER)
                .status(ClubMemberStatusEnum.PENDING)
                .joinedAt(Instant.now())
                .build();

         clubMemberRepository.save(clubMember);
        notificationService.sendToAccount(club.getOwner(), "Yêu cầu gia nhập CLB","Câu lạc bộ: "+club.getName()+ " có yêu cầu gia nhập","/my-clubs/"+club.getSlug()+"?tab=members");
         return "Tham gia thành công";
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

    public PagedResponse<MemberResponse> getMembers(int page, int size, ClubMemberStatusEnum memberStatusEnum, String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Account not found"));
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Club not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").ascending());

        // Lấy danh sách clubMembers có phân trang
        Page<ClubMember> clubMembersPage = clubMemberRepository.findPagedByClubIdAndStatus(club.getId(), memberStatusEnum, pageable);

        // Filter bỏ account hiện tại
        List<MemberResponse> memberResponses = clubMembersPage.getContent()
                .stream()
//                .filter(member -> !member.getAccount().equals(account))
                .map(clubMember -> MemberResponse.builder()
                        .id(clubMember.getId())
                        .name(clubMember.getAccount().getUserInfo().getFullName())
                        .avatar(clubMember.getAccount().getUserInfo().getAvatarUrl() != null
                                ? fileStorageService.getFileUrl(clubMember.getAccount().getUserInfo().getAvatarUrl(), "/avatar")
                                : null)
                        .joinedAt(clubMember.getJoinedAt())
                        .status(clubMember.getStatus())
                        .role(clubMember.getRole())
                        .slug(clubMember.getAccount().getUserInfo().getSlug())
                        .build()
                )
                .toList();

        return new PagedResponse<>(
                memberResponses,
                clubMembersPage.getNumber(),
                clubMembersPage.getSize(),
                clubMembersPage.getTotalElements(),
                clubMembersPage.getTotalPages(),
                clubMembersPage.isLast()
        );
    }

    public DetailMemberResponse getDetailMember(String id) {
        ClubMember clubMember = clubMemberRepository.findById(id).orElseThrow(() -> new InvalidDataException("ClubMember not found"));
        PlayerRating playerRating = playerRatingRepository.findByAccount(clubMember.getAccount()).orElse(null);    
        return DetailMemberResponse.builder()
                .id(clubMember.getId())
                .email(clubMember.getAccount().getEmail())
                .phone(clubMember.getAccount().getUserInfo().getPhone())
                .fullName(clubMember.getAccount().getUserInfo().getFullName())
                .gender(clubMember.getAccount().getUserInfo().getGender())
                .bio(clubMember.getAccount().getUserInfo().getBio())
                .createdAt(clubMember.getAccount().getCreatedAt())
                .birthDate(clubMember.getAccount().getUserInfo().getBirthDate())
                .address(clubMember.getAccount().getUserInfo().getAddress())
                .note(clubMember.getNote())
                .avatarUrl(clubMember.getAccount().getUserInfo().getAvatarUrl() != null
                        ? fileStorageService.getFileUrl(clubMember.getAccount().getUserInfo().getAvatarUrl(), "/avatar")
                        : null)
                .experience(playerRating != null ? playerRating.getExperience() : null)
                .averageTechnicalScore(playerRating != null ? playerRating.getAverageTechnicalScore() : null)
                .tactics(playerRating != null ? playerRating.getTactics() : null)
                .stamina(playerRating != null ? playerRating.getStamina() : null)
                .overallScore(playerRating != null ? playerRating.getOverallScore() : null)
                .skillLevel(playerRating != null ? playerRating.getSkillLevel() : "")
                .slug(clubMember.getAccount().getUserInfo().getSlug())
                .reputationScore(clubMember.getAccount().getReputationScore())
                .totalParticipatedEvents(clubMember.getAccount().getTotalParticipatedEvents())
                .build();
    }
}
