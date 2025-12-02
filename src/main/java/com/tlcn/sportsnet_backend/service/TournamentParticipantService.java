package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentParticipantResponse;
import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentPartnerInvitationRequest;
import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentPartnerInvitationUpdate;
import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentTeamResponse;
import com.tlcn.sportsnet_backend.entity.*;
import com.tlcn.sportsnet_backend.enums.InvitationStatusEnum;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class TournamentParticipantService {
    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final AccountRepository accountRepository;
    private final TournamentCategoryRepository tournamentCategoryRepository;
    private final FileStorageService fileStorageService;
    private final TournamentPartnerInvitationRepository tournamentPartnerInvitationRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final NotificationService notificationService;
    private final TournamentRepository tournamentRepository;
    public String joinSingle(String categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));

        TournamentCategory tournamentCategory = tournamentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy hạng mục thi đấu"));

        Tournament tournament = tournamentCategory.getTournament();

        // Kiểm tra thời gian đăng ký hợp lệ
        LocalDateTime now = LocalDateTime.now();
        if (tournament.getRegistrationStartDate() != null && tournament.getRegistrationEndDate() != null) {
            if (now.isBefore(tournament.getRegistrationStartDate())) {
                throw new InvalidDataException("Giải đấu chưa mở đăng ký");
            }
            if (now.isAfter(tournament.getRegistrationEndDate())) {
                throw new InvalidDataException("Giải đấu đã hết thời gian đăng ký");
            }
        }

        // Kiểm tra người chơi đã đăng ký hạng mục này chưa
        boolean alreadyJoined = tournamentParticipantRepository.existsByAccountAndCategory(account, tournamentCategory);
        if (alreadyJoined) {
            throw new InvalidDataException("Bạn đã đăng ký hạng mục này rồi");
        }

        // Kiểm tra số lượng người tham gia
        int currentCount = tournamentParticipantRepository.countByCategory(tournamentCategory);
        if (tournamentCategory.getMaxParticipants() != null
                && currentCount >= tournamentCategory.getMaxParticipants()) {
            throw new InvalidDataException("Hạng mục này đã đủ số lượng người tham gia");
        }

        TournamentParticipant tournamentParticipant = TournamentParticipant.builder()
                .account(account)
                .status(TournamentParticipantEnum.PENDING)
                .category(tournamentCategory)
                .build();

        tournamentParticipantRepository.save(tournamentParticipant);

        return "Đã đăng ký tham gia thành công";
    }

    public String joinDouble(String categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));

        TournamentCategory tournamentCategory = tournamentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy hạng mục thi đấu"));
        Tournament tournament = tournamentCategory.getTournament();

        // Kiểm tra thời gian đăng ký hợp lệ
        LocalDateTime now = LocalDateTime.now();
        if (tournament.getRegistrationStartDate() != null && tournament.getRegistrationEndDate() != null) {
            if (now.isBefore(tournament.getRegistrationStartDate())) {
                throw new InvalidDataException("Giải đấu chưa mở đăng ký");
            }
            if (now.isAfter(tournament.getRegistrationEndDate())) {
                throw new InvalidDataException("Giải đấu đã hết thời gian đăng ký");
            }
        }
        // Kiểm tra người chơi đã đăng ký hạng mục này chưa
        boolean alreadyJoined = tournamentTeamRepository.existsByAccountAndCategory( tournamentCategory.getId(), account, TournamentParticipantEnum.DRAFT);
        if (alreadyJoined) {
            throw new InvalidDataException("Bạn đã đăng ký hạng mục này rồi");
        }

        TournamentTeam tournamentTeam = tournamentTeamRepository.findByCategoryAndAccount(tournamentCategory.getId(), account).orElseThrow(()-> new InvalidDataException("KHông tìm thấy đội đã đăng ký"));
        tournamentTeam.setStatus(TournamentParticipantEnum.PENDING);
        tournamentTeamRepository.save(tournamentTeam);

        return "Đã đăng ký tham gia thành công";
    }

    public PagedResponse<TournamentParticipantResponse> getAllParticipants(String categoryId, List<TournamentParticipantEnum> status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TournamentParticipant> participantPage;

        if (status == null || status.isEmpty()) {
            participantPage = tournamentParticipantRepository.findByCategoryId(categoryId, pageable);
        } else {
            participantPage = tournamentParticipantRepository.findByCategoryIdAndStatusIn(categoryId, status, pageable);
        }

        List<TournamentParticipantResponse> content = participantPage
                .getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                participantPage.getNumber(),
                participantPage.getSize(),
                participantPage.getTotalElements(),
                participantPage.getTotalPages(),
                participantPage.isLast()
        );
    }

    public PagedResponse<TournamentTeamResponse> getAllTeamParticipants(String categoryId, List<TournamentParticipantEnum> status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<TournamentTeam> participantPage;

        if (status == null || status.isEmpty()) {
            participantPage = tournamentTeamRepository.findByCategoryId(categoryId, pageable);
        } else {
            participantPage = tournamentTeamRepository.findByCategoryIdAndStatusIn(categoryId, status, pageable);
        }

        List<TournamentTeamResponse> content = participantPage
                .getContent()
                .stream()
                .map(this::toTeamResponse)
                .toList();

        return new PagedResponse<>(
                content,
                participantPage.getNumber(),
                participantPage.getSize(),
                participantPage.getTotalElements(),
                participantPage.getTotalPages(),
                participantPage.isLast()
        );
    }

    private TournamentParticipantResponse toResponse(TournamentParticipant p ) {
        return TournamentParticipantResponse.builder()
                .id(p.getId())
                .fullName(p.getAccount().getUserInfo().getFullName())
                .slug(p.getAccount().getUserInfo().getSlug())
                .avatarUrl(fileStorageService.getFileUrl(p.getAccount().getUserInfo().getAvatarUrl(), "/avatar"))
                .email(p.getAccount().getEmail())
                .gender(p.getAccount().getUserInfo().getGender())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }

    @Transactional
    public void updateParticipantStatus(String participantId, TournamentParticipantEnum newStatus) {
        TournamentParticipant participant = tournamentParticipantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        if (participant.getStatus() == TournamentParticipantEnum.APPROVED ||
                participant.getStatus() == TournamentParticipantEnum.REJECTED) {
            throw new RuntimeException("This participant has already been processed.");
        }

        participant.setStatus(newStatus);
        tournamentParticipantRepository.save(participant);
    }

    @Transactional
    public void updateTeamStatus(String participantId, TournamentParticipantEnum newStatus) {
        TournamentTeam tournamentTeam = tournamentTeamRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        if (tournamentTeam.getStatus() == TournamentParticipantEnum.APPROVED ||
                tournamentTeam.getStatus() == TournamentParticipantEnum.REJECTED) {
            throw new RuntimeException("This participant has already been processed.");
        }

        tournamentTeam.setStatus(newStatus);
        tournamentTeamRepository.save(tournamentTeam);
    }

    public void invitePartner(TournamentPartnerInvitationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        Account invitee = accountRepository.findById(request.getInviteeId())
                .orElseThrow(()-> new InvalidDataException("Không tìm thấy tài khoản người được mời"));
        TournamentCategory tournamentCategory = tournamentCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(()-> new InvalidDataException("KHông tìm thấy nội dung giải đấu"));
        int currentCount = tournamentParticipantRepository.countByCategory(tournamentCategory);
        if (tournamentCategory.getMaxParticipants() != null
                && currentCount >= tournamentCategory.getMaxParticipants()) {
            throw new InvalidDataException("Hạng mục này đã đủ số lượng người tham gia");
        }
        TournamentPartnerInvitation tournamentPartnerInvitation =  TournamentPartnerInvitation.builder()
                .inviter(account)
                .invitee(invitee)
                .message(request.getMessage())
                .status(InvitationStatusEnum.PENDING)
                .category(tournamentCategory  )
                .build();
        tournamentPartnerInvitationRepository.save(tournamentPartnerInvitation);
        notificationService.sendToAccount(invitee.getEmail(),account.getUserInfo().getFullName()+" đã mời bạn ghép đội", account.getUserInfo().getFullName() +" đã mời bạn tham gia nội dung "+ tournamentCategory.getCategory().getLabel() +" giải đấu "+tournamentCategory.getTournament().getName(), "/tournaments/"+tournamentCategory.getTournament().getSlug()+"/categories/"+tournamentCategory.getId() );
    }
    @Transactional
    public void updatePartnerStatus(TournamentPartnerInvitationUpdate request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy tài khoản"));
        TournamentPartnerInvitation tournamentPartnerInvitation = tournamentPartnerInvitationRepository.findById(request.getId())
                .orElseThrow(()-> new InvalidDataException("Không tìm thấy lời mời"));
        if(account.getId() != tournamentPartnerInvitation.getInvitee().getId()) {
            throw new InvalidDataException("Không đúng quyền");
        }
        Tournament tournament = tournamentRepository.findByCategoryId(tournamentPartnerInvitation.getCategory().getId())
                .orElseThrow(()-> new InvalidDataException("Không tìm thấy giải đấu" ));

        if(request.getStatus() == InvitationStatusEnum.ACCEPTED){
            TournamentTeam tournamentTeam = TournamentTeam.builder()
                    .player1(tournamentPartnerInvitation.getInviter())
                    .player2(tournamentPartnerInvitation.getInvitee())
                    .category(tournamentPartnerInvitation.getCategory())
                    .status(TournamentParticipantEnum.DRAFT)
                    .build();
            tournamentTeamRepository.save(tournamentTeam);
            notificationService.sendToAccount(tournamentPartnerInvitation.getInviter().getEmail(),tournamentPartnerInvitation.getInvitee().getUserInfo().getFullName()+" đã chấp nhận lời mời tham gia", tournamentPartnerInvitation.getInvitee().getUserInfo().getFullName() +" đã chấp nhận tham gia nội dung "+ tournamentPartnerInvitation.getCategory().getCategory().getLabel() +" giải đấu "+tournament.getName(), "/tournaments/"+tournament.getSlug()+"/categories/"+tournamentTeam.getCategory().getId() );
        }
        else{
            notificationService.sendToAccount(tournamentPartnerInvitation.getInviter().getEmail(),tournamentPartnerInvitation.getInvitee().getUserInfo().getFullName()+" đã từ chối lời mời tham gia", tournamentPartnerInvitation.getInvitee().getUserInfo().getFullName() +" đã từ chối tham gia nội dung "+ tournamentPartnerInvitation.getCategory().getCategory().getLabel() +" giải đấu "+tournament.getName(), "/tournaments/"+tournament.getSlug()+"/categories/"+tournamentPartnerInvitation.getCategory().getId() );
        }
        deleteInvitation(tournamentPartnerInvitation.getId());
    }

    public void deleteInvitation(String id){
        tournamentPartnerInvitationRepository.deleteById(id);
    }
    public TournamentTeamResponse toTeamResponse(TournamentTeam tournamentTeam) {
        return TournamentTeamResponse.builder()
                .id(tournamentTeam.getId())
                .teamName(tournamentTeam.getTeamName())
                .status(tournamentTeam.getStatus())
                .player1Email(tournamentTeam.getPlayer1().getEmail())
                .player2Email(tournamentTeam.getPlayer2().getEmail())
                .player1FullName(tournamentTeam.getPlayer1().getUserInfo().getFullName())
                .player2FullName(tournamentTeam.getPlayer2().getUserInfo().getFullName())
                .player1Gender(tournamentTeam.getPlayer1().getUserInfo().getGender())
                .player2Gender(tournamentTeam.getPlayer2().getUserInfo().getGender())
                .player1Slug(tournamentTeam.getPlayer1().getUserInfo().getSlug())
                .player2Slug(tournamentTeam.getPlayer2().getUserInfo().getSlug())
                .player1AvatarUrl(fileStorageService.getFileUrl(tournamentTeam.getPlayer1().getUserInfo().getAvatarUrl(), "/avatar"))
                .player2AvatarUrl(fileStorageService.getFileUrl(tournamentTeam.getPlayer2().getUserInfo().getAvatarUrl(), "/avatar"))
                .createdAt(tournamentTeam.getCreatedAt())
                .build();
    }
}
