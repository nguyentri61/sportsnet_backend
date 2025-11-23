package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.TournamentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentPaymentRepository extends JpaRepository<TournamentPayment, String> {
    Optional<TournamentPayment> findByTxnRef(String txnRef);
}
