package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, String> {
}
