package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Tournament;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.TournamentCategoryRepository;
import com.tlcn.sportsnet_backend.repository.TournamentParticipantRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TournamentParticipantService {
    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final AccountRepository accountRepository;
    private final TournamentCategoryRepository tournamentCategoryRepository;

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
}
