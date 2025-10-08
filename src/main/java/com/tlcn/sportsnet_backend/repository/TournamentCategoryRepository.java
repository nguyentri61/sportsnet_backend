package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentCategoryRepository extends JpaRepository<TournamentCategory, String> {
}
