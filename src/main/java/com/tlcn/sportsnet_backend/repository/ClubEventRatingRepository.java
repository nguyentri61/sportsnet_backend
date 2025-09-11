package com.tlcn.sportsnet_backend.repository;


import com.tlcn.sportsnet_backend.entity.ClubEventRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubEventRatingRepository extends JpaRepository<ClubEventRating, String> {
    List<ClubEventRating> findByClubEventIdOrderByCreatedAtDesc(String clubEventId);
    Optional<ClubEventRating> findByClubEventIdAndAccountId(String clubEventId, String accountId);
}
