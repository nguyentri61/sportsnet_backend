package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.MessageStatus;
import com.tlcn.sportsnet_backend.enums.MessageSeenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageStatusRepository extends JpaRepository<MessageStatus, String> {
    List<MessageStatus> findByAccountIdAndMessageConversationIdAndStatus(
            String accountId,
            String conversationId,
            MessageSeenStatus status
    );

    @Query("SELECT COUNT(ms) FROM MessageStatus ms " +
            "WHERE ms.account.id = :accountId " +
            "AND ms.message.conversation.id = :conversationId " +
            "AND ms.status = 'UNSEEN'")
    Long countUnreadMessagesByConversation(String accountId, String conversationId);

}
