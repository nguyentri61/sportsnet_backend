package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.ClubEvent;
import com.tlcn.sportsnet_backend.entity.ClubEventParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubEventParticipantRepository extends JpaRepository<ClubEventParticipant, String> {
    boolean existsByClubEventAndParticipant(ClubEvent clubEvent, Account account);

    Page<ClubEventParticipant> findAllByClubEvent(ClubEvent clubEvent, Pageable pageable);

    Page<ClubEventParticipant> findByParticipant_Id(String accountId, Pageable pageable);
}
