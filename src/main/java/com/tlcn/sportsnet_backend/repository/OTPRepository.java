package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPRepository  extends JpaRepository<OTP, String> {

    Optional<OTP> findByAccount_email(String email);
}
