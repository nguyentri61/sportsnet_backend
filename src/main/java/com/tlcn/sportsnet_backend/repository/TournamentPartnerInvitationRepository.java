package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.TournamentParticipant;
import com.tlcn.sportsnet_backend.entity.TournamentPartnerInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentPartnerInvitationRepository extends JpaRepository<TournamentPartnerInvitation, String> {
    @Query("""
    SELECT i FROM TournamentPartnerInvitation i
    WHERE i.category.id = :categoryId
      AND (i.inviter = :account OR i.invitee = :account)
""")
    List<TournamentPartnerInvitation> findByCategoryAndAccount(
            @Param("categoryId") String categoryId,
            @Param("account") Account account
    );


}
