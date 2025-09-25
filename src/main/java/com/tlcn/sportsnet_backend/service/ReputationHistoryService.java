package com.tlcn.sportsnet_backend.service;

import com.tlcn.sportsnet_backend.dto.reputationHistory.ReputationHistoryResponse;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ReputationHistory;
import com.tlcn.sportsnet_backend.error.InvalidDataException;
import com.tlcn.sportsnet_backend.repository.AccountRepository;
import com.tlcn.sportsnet_backend.repository.ReputationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReputationHistoryService {
   private final ReputationHistoryRepository reputationHistoryRepository;
    private final AccountRepository accountRepository;
    public List<ReputationHistoryResponse> getAll() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByEmail(authentication.getName()).orElseThrow(() -> new InvalidDataException("Account not found"));
        List<ReputationHistory> reputationHistories = reputationHistoryRepository.findByAccountOrderByCreatedAtDesc(account);
        return  reputationHistories.stream()
                .map(this::toReputationHistoryResponse)
                .toList();

    }
    public ReputationHistoryResponse toReputationHistoryResponse(ReputationHistory reputationHistory) {
        return ReputationHistoryResponse.builder()
                .id(reputationHistory.getId())
                .change(reputationHistory.getChange())
                .reason(reputationHistory.getReason())
                .createdAt(reputationHistory.getCreatedAt())
                .build();
    }
}
