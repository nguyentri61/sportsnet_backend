package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.entity.ClubMember;
import com.tlcn.sportsnet_backend.enums.ClubMemberStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubMemberRepository extends JpaRepository<ClubMember, String> {
    boolean existsByClubAndAccount(Club club, Account account);
    boolean existsByClubAndAccountAndStatus(Club club, Account account, ClubMemberStatusEnum status);
    ClubMember findByClubAndAccount(Club club, Account account);
    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.id = :clubId")
    Page<ClubMember> findPagedByClubId(String clubId, Pageable pageable);
    @Query("""
        SELECT cm FROM ClubMember cm 
        WHERE cm.club.id = :clubId 
          AND (:status IS NULL OR cm.status = :status)
    """)
    Page<ClubMember> findPagedByClubIdAndStatus(
            @Param("clubId") String clubId,
            @Param("status") ClubMemberStatusEnum status,
            Pageable pageable
    );
    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.id = :clubId")
    List<ClubMember> findByClubId(String clubId);
    @Query("SELECT cm FROM ClubMember cm WHERE cm.club.id = :clubId AND cm.status = :status")
    List<ClubMember> findByClubIdAndStatus(@Param("clubId") String clubId,
                                           @Param("status") ClubMemberStatusEnum status);

    ClubMember findClubMemberByAccountAndClub(Account account, Club club);

}
