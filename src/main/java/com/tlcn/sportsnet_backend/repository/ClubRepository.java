package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.enums.ClubMemberRoleEnum;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubStatusEnum;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.beans.Visibility;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, String>, JpaSpecificationExecutor<Club> {
    Page<Club> findAllByVisibilityAndStatus(ClubVisibilityEnum visibility, ClubStatusEnum status, Pageable pageable);
    Optional<Club> findBySlug(String slug);

    @Query("SELECT c FROM Club c " +
            "WHERE c.visibility = :visibility " +
            "AND c.status = :status " +
            "AND c.id NOT IN (SELECT cm.club.id FROM ClubMember cm WHERE cm.account = :account)")
    Page<Club> findAvailableClubsForUserAndStatus(
            @Param("visibility") ClubVisibilityEnum visibility,
            @Param("status") ClubStatusEnum status,
            @Param("account") Account account,
            Pageable pageable);

    @Query("SELECT c FROM Club c " +
            "WHERE (c.owner = :account OR EXISTS (SELECT cm FROM ClubMember cm WHERE cm.club = c AND cm.account = :account)) " +
            "AND c.status = :status")
    Page<Club> findAvailableClubsBelongUserAndStatus(
            @Param("account") Account account,
            @Param("status") ClubStatusEnum status,
            Pageable pageable);

    @Query("""
    SELECT cm.club.id
    FROM ClubMember cm
    WHERE cm.account = :account
      AND cm.role = :role
      AND cm.status = :status
""")
    List<String> findActiveMemberClubIds(
            @Param("account") Account account,
            @Param("role") ClubMemberRoleEnum role,
            @Param("status") ClubMemberStatusEnum status
    );

    List<Club> findAllByOwnerAndStatusOrderByReputationDesc(Account owner, ClubStatusEnum status );

    @Query("""
    SELECT c FROM Club c
    WHERE EXISTS (SELECT cm FROM ClubMember cm WHERE cm.club = c AND cm.account = :account AND cm.status = :status AND cm.role = :role)
    AND c.status = :clubStatus
""")
    List<Club> findClubsForUserAndStatus(
            @Param("account") Account account,
            @Param("status") ClubMemberStatusEnum status,
            @Param("role") ClubMemberRoleEnum role,
            @Param("clubStatus") ClubStatusEnum clubStatus

    );
}
