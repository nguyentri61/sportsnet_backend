package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ReputationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, String> {
}
