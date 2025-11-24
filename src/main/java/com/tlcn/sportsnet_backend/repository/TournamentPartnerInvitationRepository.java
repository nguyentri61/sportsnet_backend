package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.entity.TournamentPartnerInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentPartnerInvitationRepository extends JpaRepository<TournamentPartnerInvitation, String> {
}
