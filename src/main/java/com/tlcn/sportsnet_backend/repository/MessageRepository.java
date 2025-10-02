package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.ConversationParticipant;
import com.tlcn.sportsnet_backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository  extends JpaRepository<Message, String> {
    List<Message> findAllByConversationIdOrderByCreatedAtAsc(String conversationId);
    Page<Message> findAllByConversationId(String conversationId, Pageable pageable);
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);
}
