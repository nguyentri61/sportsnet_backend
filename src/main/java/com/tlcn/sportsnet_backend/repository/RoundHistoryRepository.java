package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.PlayerTournamentHistory;
import com.tlcn.sportsnet_backend.entity.RoundHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoundHistoryRepository extends JpaRepository<RoundHistory, String> {
    boolean existsByPlayerHistoryIdAndRound(String playerHistoryId, Integer round);
    List<RoundHistory> findByPlayerHistoryIdOrderByRoundAsc(String playerHistoryId);
}
