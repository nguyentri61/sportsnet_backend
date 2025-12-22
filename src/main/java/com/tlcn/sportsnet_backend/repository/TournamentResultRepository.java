package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentResultRepository extends JpaRepository<TournamentResult, String> {
    List<TournamentResult> findByCategory(TournamentCategory category);

    void deleteByCategory(TournamentCategory category);
}
