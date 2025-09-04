package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ClubEvent;

import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ClubEventRepository extends JpaRepository<ClubEvent, String> {
    Page<ClubEvent> findByClub_Id(String clubId, Pageable pageable);
    Page<ClubEvent> findAllByOpenForOutsideAndStatusAndDeadlineAfter(Pageable pageable, Boolean openForOutside, EventStatusEnum status, LocalDateTime deadline);
    Page<ClubEvent> findByClub_Members_Account_IdAndClub_Members_StatusAndStatusAndDeadlineAfter(
            String accountId,
            ClubMemberStatusEnum memberStatus,
            EventStatusEnum eventStatus,
            LocalDateTime now,
            Pageable pageable
    );
}
