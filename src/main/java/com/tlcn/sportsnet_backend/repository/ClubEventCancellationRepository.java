package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventCancellation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubEventCancellationRepository extends JpaRepository<ClubEventCancellation, String> {
    List<ClubEventCancellation> findByParticipant_ClubEventOrderByRequestedAtDesc(ClubEvent clubEvent);
}
