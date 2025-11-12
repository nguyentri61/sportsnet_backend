package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.tournament_participants.TournamentParticipantResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Tournament;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.payload.response.PagedResponse;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentParticipantRepository;
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
}
