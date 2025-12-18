package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.PlayerTournamentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerTournamentHistoryRepository extends JpaRepository<PlayerTournamentHistory, String> {

    List<PlayerTournamentHistory> findByPlayerIdOrderByCreatedAtDesc(String playerId);

    List<PlayerTournamentHistory> findByCategoryId(String categoryId);

    boolean existsByMatchId(String matchId);
}
