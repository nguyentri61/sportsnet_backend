package com.tlcn.sportsnet_backend.repository;

import com.tlcn.sportsnet_backend.entity.Account;
import com.tlcn.sportsnet_backend.entity.Friendship;
import com.tlcn.sportsnet_backend.enums.FriendStatusEnum;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, String> {

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :a AND f.receiver = :b) OR (f.requester = :b AND f.receiver = :a)")
    Optional<Friendship> findBetween(@Param("a") Account a, @Param("b") Account b);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :account OR f.receiver = :account) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriends(@Param("account") Account account);

    List<Friendship> findAllFriendsByReceiverAndStatus(Account receiver, FriendStatusEnum status, Sort sort);
    @Query(value = """
    SELECT COUNT(*) FROM (
        SELECT DISTINCT 
            CASE 
                WHEN f1.requester_id = :accountId1 THEN f1.receiver_id 
                ELSE f1.requester_id 
            END AS friend_id
        FROM friendships f1
        WHERE (f1.requester_id = :accountId1 OR f1.receiver_id = :accountId1)
          AND f1.status = 'ACCEPTED'
        INTERSECT
        SELECT DISTINCT 
            CASE 
                WHEN f2.requester_id = :accountId2 THEN f2.receiver_id 
                ELSE f2.requester_id 
            END AS friend_id
        FROM friendships f2
        WHERE (f2.requester_id = :accountId2 OR f2.receiver_id = :accountId2)
          AND f2.status = 'ACCEPTED'
    ) AS mutual_friends
    """, nativeQuery = true)
    long countMutualFriends(@Param("accountId1") String accountId1,
                            @Param("accountId2") String accountId2);

    @Query("""
        SELECT COUNT(f) > 0
        FROM Friendship f
        WHERE (
            (f.requester.id = :userId1 AND f.receiver.id = :userId2)
            OR
            (f.requester.id = :userId2 AND f.receiver.id = :userId1)
        )
        AND f.status = 'ACCEPTED'
    """)
    boolean areFriends(@Param("userId1") String userId1, @Param("userId2") String userId2);

}
