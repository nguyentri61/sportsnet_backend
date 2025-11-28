package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import com.tlcn.sportsnet_backend.entity.TournamentPartnerInvitation;
import com.tlcn.sportsnet_backend.entity.TournamentTeam;
import com.tlcn.sportsnet_backend.enums.TournamentParticipantEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, String> {
    @Query("""
    SELECT i FROM TournamentTeam i
    WHERE i.category.id = :categoryId
      AND (i.player1 = :account OR i.player2 = :account)
""")
    Optional<TournamentTeam> findByCategoryAndAccount(
            @Param("categoryId") String categoryId,
            @Param("account") Account account
    );
    @Query("""
    SELECT i FROM TournamentTeam i
    WHERE i.category.id = :categoryId
      AND (i.player1 = :account OR i.player2 = :account) AND i.status != :status
""")
    boolean existsByAccountAndCategory(@Param("categoryId") String categoryId,
                                       @Param("account") Account account,
                                       @Param("status") TournamentParticipantEnum status);

}
