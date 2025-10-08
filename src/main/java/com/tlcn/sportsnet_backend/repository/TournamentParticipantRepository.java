package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, String> {
}
