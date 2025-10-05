package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Conversation;
import com.tlcn.sportsnet_backend.enums.ConversationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    @Query("""
        SELECT c FROM Conversation c
        JOIN c.participants p1
        JOIN c.participants p2
        WHERE c.type = :status
          AND p1.account.id = :userA
          AND p2.account.id = :userB
    """)
    Optional<Conversation> findPrivateBetweenUsers(@Param("userA") String userA,
                                                   @Param("userB") String userB, @Param("status") ConversationType status
    );

    @Query("""
    SELECT c FROM Conversation c
    JOIN c.participants p
    WHERE p.account.id = :user
    ORDER BY c.updatedAt DESC
""")
    List<Conversation> findAllByUserOrderByUpdated(@Param("user") String user);

    @Query("""
        SELECT COUNT(c) > 0
        FROM Conversation c
        JOIN c.participants p
        WHERE c.id = :conversationId
          AND p.account.id = :userId
    """)
    boolean existsByIdAndUser(@Param("conversationId") String conversationId,
                              @Param("userId") String userId);


    Optional<Conversation> findByClubId(String clubId);
}
