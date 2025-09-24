package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClubEventParticipantRepository extends JpaRepository<ClubEventParticipant, String> {
    boolean existsByClubEventAndParticipant(ClubEvent clubEvent, Account account);

    Page<ClubEventParticipant> findAllByClubEvent(ClubEvent clubEvent, Pageable pageable);
    Optional<ClubEventParticipant> findByClubEventAndParticipant(ClubEvent clubEvent, Account account);
    Optional<ClubEventParticipant> findByClubEvent_IdAndParticipant(String id, Account account);
    List<ClubEventParticipant> findAllByClubEventOrderByJoinedAtDesc(ClubEvent clubEvent);
    Page<ClubEventParticipant> findByParticipant_Id(String accountId, Pageable pageable);
}
