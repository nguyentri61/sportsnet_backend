package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.entity.Account;
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

@Service
@AllArgsConstructor
public class TournamentParticipantService {
    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final AccountRepository accountRepository;
    private final TournamentCategoryRepository tournamentCategoryRepository;
    public Object joinSingle(String categoryId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        TournamentCategory tournamentCategory = tournamentCategoryRepository.findById(categoryId).orElseThrow(() -> new InvalidDataException("TournamentCategory not found"));
        TournamentParticipant tournamentParticipant = TournamentParticipant.builder()
                .account(account)
                .status(TournamentParticipantEnum.PENDING)
                .category(tournamentCategory).build();
        tournamentParticipantRepository.save(tournamentParticipant);
        return "Đã đăng ký tham gia thành công";
    }
}
