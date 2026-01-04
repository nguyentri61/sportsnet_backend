package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.PlayerTournamentHistory;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerTournamentHistoryRepository extends JpaRepository<PlayerTournamentHistory, String> {
    Optional<PlayerTournamentHistory> findByTeamIdAndCategoryId(String teamId, String categoryId);
    List<PlayerTournamentHistory> findByPlayerIdOrderByCreatedAtDesc(String playerId);
    Optional<PlayerTournamentHistory> findByPlayerIdAndCategoryId(String playerId, String categoryId);

}
