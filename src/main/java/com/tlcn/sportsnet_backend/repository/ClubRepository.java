package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Club;
import com.tlcn.sportsnet_backend.enums.ClubVisibilityEnum;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.beans.Visibility;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, String> {
    Page<Club> findAllByVisibility(ClubVisibilityEnum visibility, Pageable pageable);

    @Query("""
    SELECT c FROM Club c
    WHERE c.visibility = :visibility
      AND c.owner <> :account
      AND c.id NOT IN (
          SELECT cm.club.id FROM ClubMember cm WHERE cm.account = :account
      )
""")
    Page<Club> findAvailableClubsForUser(
            @Param("visibility") ClubVisibilityEnum visibility,
            @Param("account") Account account,
            Pageable pageable
    );

    @Query("""
    SELECT c FROM Club c
    WHERE  (
          c.id IN (
              SELECT cm.club.id FROM ClubMember cm WHERE cm.account = :account
          )
      )
""")
    Page<Club> findAvailableClubsBelongUser(

            @Param("account") Account account,
            Pageable pageable
    );


}
