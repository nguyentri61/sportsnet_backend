package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ClubEvent;

import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.EventStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubEventRepository extends JpaRepository<ClubEvent, String> {
    Page<ClubEvent> findByClub_Slug(String slug, Pageable pageable);

    Optional<ClubEvent> findBySlug(String slug);

    Page<ClubEvent> findAllByOpenForOutsideAndStatusAndDeadlineAfter(Pageable pageable, Boolean openForOutside, EventStatusEnum status, LocalDateTime deadline);
    List<ClubEvent> findAllByOpenForOutsideAndStatusAndDeadlineAfterOrderByCreatedAtDesc(Boolean openForOutside, EventStatusEnum status, LocalDateTime deadline);

    Page<ClubEvent> findByClub_Members_Account_IdAndClub_Members_StatusAndStatusAndDeadlineAfter(
            String accountId,
            ClubMemberStatusEnum memberStatus,
            EventStatusEnum eventStatus,
            LocalDateTime now,
            Pageable pageable
    );

    @Query("SELECT COUNT(e) FROM ClubEvent e WHERE e.club.id = :clubId AND e.status = :status")
    Long  countByClubIdAndStatus(@Param("clubId") String clubId,
                                @Param("status") EventStatusEnum status);

    @Query("SELECT MAX(c.eventCount) FROM (SELECT COUNT(e) as eventCount FROM ClubEvent e WHERE e.status = :status GROUP BY e.club.id) c")
    Long  findMaxEventCount(@Param("status") EventStatusEnum status);

    @Query("SELECT SUM(e.totalMember) " +
            "FROM ClubEvent e " +
            "WHERE e.club.id = :clubId")
    Long  sumTotalMemberByClubId(@Param("clubId") String clubId);
}
