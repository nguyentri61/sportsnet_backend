package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.PlayerRating;
import com.tlcn.sportsnet_backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRatingRepository extends JpaRepository<PlayerRating, String> {
    Optional<PlayerRating> findByAccount(Account account);
}
