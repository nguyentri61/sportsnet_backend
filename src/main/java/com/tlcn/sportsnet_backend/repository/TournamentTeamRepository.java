package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, String> {
}
