package com.tlcn.sportsnet_backend.repository;


import com.tlcn.sportsnet_backend.entity.Conversation;
import com.tlcn.sportsnet_backend.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, String> {
    Optional<ConversationParticipant> findByConversationIdAndAccountId(String conversationId, String accountId);
}
