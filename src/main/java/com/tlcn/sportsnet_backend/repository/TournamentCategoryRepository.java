package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Tournament;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentCategoryRepository extends JpaRepository<TournamentCategory, String> {
    List<TournamentCategory> findByTournament(Tournament tournament);
}
