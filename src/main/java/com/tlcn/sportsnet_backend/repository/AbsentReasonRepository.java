package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.AbsentReason;
import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbsentReasonRepository extends JpaRepository<AbsentReason, String> {
    boolean existsByParticipation(ClubEventParticipant participation);
    Optional<AbsentReason> findByParticipation_Id(String id);
}
