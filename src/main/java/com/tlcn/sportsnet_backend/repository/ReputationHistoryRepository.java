package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ReputationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, String> {
    List<ReputationHistory> findByAccountOrderByCreatedAtDesc(Account account);
}
