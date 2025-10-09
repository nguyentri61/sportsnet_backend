package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentResultRepository extends JpaRepository<TournamentResult, String> {
}
